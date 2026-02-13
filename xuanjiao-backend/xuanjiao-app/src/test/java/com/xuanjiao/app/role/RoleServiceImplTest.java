package com.xuanjiao.app.role;

import com.xuanjiao.app.menu.MenuService;
import com.xuanjiao.app.role.impl.RoleServiceImpl;
import com.xuanjiao.client.dto.role.dto.RoleDTO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.role.RoleQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RoleServiceImpl 单元测试
 * 验证 RoleMapper 重构后 RoleService 功能正确
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleServiceImplTest {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private MenuService menuService;

    @InjectMocks
    private RoleServiceImpl roleService;

    private RoleDO testRole;
    private RoleDTO testRoleDTO;

    @BeforeEach
    public void setUp() {
        testRole = new RoleDO();
        testRole.setId(1L);
        testRole.setName("系统管理员");
        testRole.setRoleType("SYSTEM_ADMIN");
        testRole.setStatus(1);

        testRoleDTO = new RoleDTO();
        testRoleDTO.setId(1L);
        testRoleDTO.setName("系统管理员");
        testRoleDTO.setRoleType("SYSTEM_ADMIN");
    }

    // ==================== RoleMapper Call Site Tests ====================

    @Test
    @Order(1)
    public void testList_WithOrderByDesc() {
        // 测试列表查询按ID降序排列
        // This tests: RoleQuery with orderByField="id", orderByDirection="DESC" at line 28-30

        when(roleMapper.selectList(any(RoleQuery.class)))
                .thenReturn(Arrays.asList(testRole));

        List<RoleDTO> result = roleService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        // Verify RoleQuery was called with orderByField and orderByDirection
        verify(roleMapper).selectList(argThat(query ->
                query != null && "id".equals(query.getOrderByField()) && "DESC".equals(query.getOrderByDirection())
        ));
        System.out.println("✓ RoleService.list() - RoleMapper测试通过");
    }

    @Test
    @Order(2)
    public void testCreate_NewRoleType() {
        // 测试创建新角色类型（不存在）
        // This tests: RoleQuery with roleType at line 111-116 (called from create)

        when(roleMapper.selectCount(any(RoleQuery.class)))
                .thenReturn(0L);
        when(roleMapper.insert(any(RoleDO.class)))
                .thenReturn(1);

        // This should not throw exception
        assertDoesNotThrow(() -> roleService.create(testRoleDTO));

        // Verify RoleQuery was called with roleType
        verify(roleMapper).selectCount(argThat(query ->
                query != null && "SYSTEM_ADMIN".equals(query.getRoleType()) && query.getExcludeId() == null
        ));
        System.out.println("✓ RoleService.create(新角色类型) - RoleMapper测试通过");
    }

    @Test
    @Order(3)
    public void testCreate_ExistingRoleType() {
        // 测试创建已存在的角色类型
        // This tests: RoleQuery with roleType at line 111-116 (called from create)

        when(roleMapper.selectCount(any(RoleQuery.class)))
                .thenReturn(1L);

        // This should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                roleService.create(testRoleDTO)
        );

        assertTrue(exception.getMessage().contains("角色类型已存在"));

        // Verify RoleQuery was called with roleType
        verify(roleMapper).selectCount(argThat(query ->
                query != null && "SYSTEM_ADMIN".equals(query.getRoleType())
        ));
        System.out.println("✓ RoleService.create(已存在角色类型) - RoleMapper测试通过");
    }

    @Test
    @Order(4)
    public void testUpdate_WithExcludeId() {
        // 测试更新角色时排除当前ID
        // This tests: RoleQuery with roleType+excludeId at line 111-116 (called from update)

        when(roleMapper.selectCount(any(RoleQuery.class)))
                .thenReturn(0L);
        when(roleMapper.updateById(any(RoleDO.class)))
                .thenReturn(1);

        // This should not throw exception (excluding current role)
        assertDoesNotThrow(() -> roleService.update(testRoleDTO));

        // Verify RoleQuery was called with roleType and excludeId
        verify(roleMapper).selectCount(argThat(query ->
                query != null && "SYSTEM_ADMIN".equals(query.getRoleType()) && query.getExcludeId() == 1L
        ));
        System.out.println("✓ RoleService.update(排除ID) - RoleMapper测试通过");
    }

    @Test
    @Order(5)
    public void testUpdate_ExistingRoleTypeInOtherRole() {
        // 测试更新角色时，角色类型在其他角色中已存在
        // This tests: RoleQuery with roleType+excludeId at line 111-116 (called from update)

        when(roleMapper.selectCount(any(RoleQuery.class)))
                .thenReturn(1L);

        // This should throw exception (roleType exists in another role)
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                roleService.update(testRoleDTO)
        );

        assertTrue(exception.getMessage().contains("角色类型已存在"));

        // Verify RoleQuery was called with roleType and excludeId
        verify(roleMapper).selectCount(argThat(query ->
                query != null && "SYSTEM_ADMIN".equals(query.getRoleType()) && query.getExcludeId() == 1L
        ));
        System.out.println("✓ RoleService.update(其他角色已存在) - RoleMapper测试通过");
    }
}
