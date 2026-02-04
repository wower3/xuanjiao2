package com.xuanjiao.app.dept;

import com.xuanjiao.app.dept.impl.DeptServiceImpl;
import com.xuanjiao.client.dto.DeptDTO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.dept.DeptQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DeptServiceImpl 单元测试
 * 验证 DeptMapper 重构后 DeptService 功能正确
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DeptServiceImplTest {

    @Mock
    private DeptMapper deptMapper;

    @InjectMocks
    private DeptServiceImpl deptService;

    private DeptDO rootDept;
    private DeptDO childDept;

    @BeforeEach
    public void setUp() {
        // 根部门
        rootDept = new DeptDO();
        rootDept.setId(1L);
        rootDept.setCode("HEAD001");
        rootDept.setLevel(1);
        rootDept.setFullCode("HEAD001");
        rootDept.setName("总公司");
        rootDept.setParentId(0L);
        rootDept.setSort(1);
        rootDept.setStatus(1);

        // 子部门
        childDept = new DeptDO();
        childDept.setId(2L);
        childDept.setCode("BRANCH001");
        childDept.setLevel(2);
        childDept.setFullCode("HEAD001-BRANCH001");
        childDept.setName("分公司");
        childDept.setParentId(1L);
        childDept.setSort(1);
        childDept.setStatus(1);
    }

    // ==================== DeptMapper Call Site Tests ====================

    @Test
    @Order(1)
    public void testList_EmptyQuery() {
        // 测试列表查询（使用 DeptQuery）
        // This tests: DeptMapper.selectList(new DeptQuery()) at line 28-30

        when(deptMapper.selectList(any(DeptQuery.class)))
                .thenReturn(Arrays.asList(rootDept, childDept));

        List<DeptDTO> result = deptService.list();

        assertNotNull(result);
        assertEquals(2, result.size());
        // Verify DeptQuery was called
        verify(deptMapper).selectList(argThat(query ->
                query != null
        ));
        System.out.println("✓ DeptService.list() - DeptMapper测试通过");
    }

    @Test
    @Order(2)
    public void testGetTree() {
        // 测试获取树形结构
        // This tests: DeptMapper.selectAll() at line 35

        when(deptMapper.selectAll())
                .thenReturn(Arrays.asList(rootDept, childDept));

        List<DeptDTO> result = deptService.getTree();

        assertNotNull(result);
        assertEquals(1, result.size()); // 只有根节点
        assertEquals("总公司", result.get(0).getName());
        assertEquals(1, result.get(0).getChildren().size()); // 有一个子节点
        assertEquals("分公司", result.get(0).getChildren().get(0).getName());

        verify(deptMapper).selectAll();
        System.out.println("✓ DeptService.getTree() - DeptMapper测试通过");
    }

    @Test
    @Order(3)
    public void testGetById() {
        // 测试根据ID查询
        // This tests: DeptMapper.selectById() at line 41

        when(deptMapper.selectById(1L)).thenReturn(rootDept);

        DeptDTO result = deptService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("总公司", result.getName());
        assertEquals(1, result.getLevel());

        verify(deptMapper).selectById(1L);
        System.out.println("✓ DeptService.getById() - DeptMapper测试通过");
    }

    @Test
    @Order(4)
    public void testSave_WithParent() {
        // 测试保存部门（有父部门）
        // This tests: DeptMapper.selectById(), DeptMapper.insert()

        when(deptMapper.selectById(1L)).thenReturn(rootDept);
        when(deptMapper.insert(any(DeptDO.class))).thenReturn(1);
        when(deptMapper.selectByCode(anyString())).thenReturn(null); // For generateCode check

        DeptDTO dto = new DeptDTO();
        dto.setName("新部门");
        dto.setParentId(1L);
        dto.setSort(1);
        dto.setStatus(1);

        deptService.save(dto);

        // 验证查询了父部门
        verify(deptMapper).selectById(1L);
        // 验证插入了新部门（level=2, fullCode自动生成）
        verify(deptMapper).insert(argThat(dept ->
                dept.getLevel() == 2
                        && dept.getFullCode() != null
                        && dept.getFullCode().startsWith("HEAD001-") // fullCode包含父部门的fullCode
                        && "新部门".equals(dept.getName())
        ));
        System.out.println("✓ DeptService.save(有父部门) - DeptMapper测试通过");
    }

    @Test
    @Order(5)
    public void testSave_RootDept() {
        // 测试保存根部门
        // This tests: DeptMapper.insert() without parent

        when(deptMapper.insert(any(DeptDO.class))).thenReturn(1);

        DeptDTO dto = new DeptDTO();
        dto.setName("新根部门");
        dto.setParentId(0L);
        dto.setSort(1);
        dto.setStatus(1);

        deptService.save(dto);

        // 验证插入了根部门（level=1, fullCode=code）
        verify(deptMapper).insert(argThat(dept ->
                dept.getLevel() == 1
                        && dept.getCode() != null
                        && !dept.getCode().isEmpty()
                        && dept.getFullCode() != null
        ));
        System.out.println("✓ DeptService.save(根部门) - DeptMapper测试通过");
    }

    @Test
    @Order(6)
    public void testUpdate() {
        // 测试更新部门
        // This tests: DeptMapper.updateById()

        when(deptMapper.updateById(any(DeptDO.class))).thenReturn(1);

        DeptDTO dto = new DeptDTO();
        dto.setId(1L);
        dto.setName("修改后的部门");

        deptService.update(dto);

        verify(deptMapper).updateById(argThat(dept ->
                dept.getId() == 1L
                        && "修改后的部门".equals(dept.getName())
        ));
        System.out.println("✓ DeptService.update() - DeptMapper测试通过");
    }

    @Test
    @Order(7)
    public void testDelete() {
        // 测试删除部门
        // This tests: DeptMapper.deleteById()

        when(deptMapper.deleteById(1L)).thenReturn(1);

        deptService.delete(1L);

        verify(deptMapper).deleteById(1L);
        System.out.println("✓ DeptService.delete() - DeptMapper测试通过");
    }

    @Test
    @Order(8)
    public void testGenerateCode() {
        // 测试生成部门编号
        // This tests: DeptMapper.selectByCode()

        when(deptMapper.selectByCode(anyString())).thenReturn(null);

        String code = deptService.generateCode();

        assertNotNull(code);
        assertEquals(6, code.length());
        verify(deptMapper, atLeastOnce()).selectByCode(anyString());
        System.out.println("✓ DeptService.generateCode() - DeptMapper测试通过");
    }
}
