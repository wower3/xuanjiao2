package com.xuanjiao.app.user;

import com.xuanjiao.client.user.UserDTO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.dept.DeptQuery;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.user.UserQuery;
import com.xuanjiao.app.user.impl.UserServiceImpl;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * 验证 UserMapper 重构后 UserService 功能正确
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private DeptMapper deptMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDO adminUser;
    private RoleDO adminRole;
    private DeptDO adminDept;

    @BeforeEach
    public void setUp() {
        // 初始化测试数据
        adminRole = new RoleDO();
        adminRole.setId(1L);
        adminRole.setName("系统管理员");
        adminRole.setRoleType("SYSTEM_ADMIN");
        adminRole.setStatus(1);

        adminDept = new DeptDO();
        adminDept.setId(100L);
        adminDept.setName("总公司");
        adminDept.setLevel(1); // Set level for hierarchy tests

        adminUser = new UserDO();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRealName("管理员");
        adminUser.setDeptId(100L);
        adminUser.setRoleId(1L);
        adminUser.setStatus(1);
    }

    // ==================== UserServiceImpl.list() 测试 ====================

    @Test
    @Order(1)
    public void testList_EmptyQuery() {
        // 测试空查询：userMapper.selectList(new UserQuery())
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(adminUser));
        when(roleMapper.selectById(1L)).thenReturn(adminRole);
        when(deptMapper.selectById(100L)).thenReturn(adminDept);

        List<UserDTO> result = userService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userMapper, times(1)).selectList(any(UserQuery.class));
        System.out.println("✓ UserService.list() - 空查询测试通过");
    }

    @Test
    @Order(2)
    public void testListByBranchDept_WithEmptyQuery() {
        // 测试带部门的查询：当用户部门不是二级机构时，返回所有用户
        when(userMapper.selectById(1L)).thenReturn(adminUser);
        when(deptMapper.selectById(100L)).thenReturn(adminDept);
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(adminUser));
        when(roleMapper.selectById(1L)).thenReturn(adminRole);
        when(deptMapper.selectById(100L)).thenReturn(adminDept);

        List<UserDTO> result = userService.listByBranchDept(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userMapper, times(1)).selectList(any(UserQuery.class));
        System.out.println("✓ UserService.listByBranchDept() - 测试通过");
    }

    @Test
    @Order(3)
    public void testListWithFilter_NoFilters() {
        // 测试带筛选的查询：无筛选条件时调用 selectList(new DeptQuery())
        // Setup: user is SYSTEM_ADMIN with access to all depts
        when(userMapper.selectById(1L)).thenReturn(adminUser);
        when(roleMapper.selectById(1L)).thenReturn(adminRole);
        when(deptMapper.selectList(any(DeptQuery.class))).thenReturn(Arrays.asList(adminDept));
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(adminUser));
        when(roleMapper.selectById(1L)).thenReturn(adminRole);
        when(deptMapper.selectById(100L)).thenReturn(adminDept);

        List<UserDTO> result = userService.listWithFilter(1L, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userMapper, times(1)).selectList(any(UserQuery.class));
        System.out.println("✓ UserService.listWithFilter(无筛选) - 测试通过");
    }

    @Test
    @Order(4)
    public void testListWithFilter_WithRoleIds() {
        // 测试按角色筛选
        // The implementation filters in Java, not in SQL
        when(userMapper.selectById(1L)).thenReturn(adminUser);
        when(roleMapper.selectById(1L)).thenReturn(adminRole);
        when(deptMapper.selectList(any(DeptQuery.class))).thenReturn(Arrays.asList(adminDept));
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(adminUser));
        when(roleMapper.selectById(1L)).thenReturn(adminRole);
        when(deptMapper.selectById(100L)).thenReturn(adminDept);

        List<UserDTO> result = userService.listWithFilter(1L, Arrays.asList(1L), null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        // Verify selectList was called with empty query (filtering happens in Java)
        verify(userMapper).selectList(argThat(query ->
            query != null && query.getRoleIds() == null // No roleIds in SQL query
        ));
        System.out.println("✓ UserService.listWithFilter(roleIds筛选) - 测试通过 (Java过滤)");
    }

    @Test
    @Order(5)
    public void testListWithFilter_WithDeptId() {
        // 测试按部门筛选
        // The implementation filters in Java, not in SQL
        when(userMapper.selectById(1L)).thenReturn(adminUser);
        when(roleMapper.selectById(1L)).thenReturn(adminRole);
        when(deptMapper.selectList(any(DeptQuery.class))).thenReturn(Arrays.asList(adminDept));
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(adminUser));
        when(roleMapper.selectById(1L)).thenReturn(adminRole);
        when(deptMapper.selectById(100L)).thenReturn(adminDept);

        List<UserDTO> result = userService.listWithFilter(1L, null, 100L, false);

        assertNotNull(result);
        assertEquals(1, result.size());
        // Verify selectList was called with empty query (filtering happens in Java)
        verify(userMapper).selectList(argThat(query ->
            query != null && query.getDeptId() == null // No deptId in SQL query
        ));
        System.out.println("✓ UserService.listWithFilter(deptId筛选) - 测试通过 (Java过滤)");
    }

    @Test
    @Order(6)
    public void testGetById() {
        // 测试通过ID获取用户
        when(userMapper.selectById(1L)).thenReturn(adminUser);
        when(roleMapper.selectById(1L)).thenReturn(adminRole);
        when(deptMapper.selectById(100L)).thenReturn(adminDept);

        UserDTO result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("admin", result.getUsername());
        assertEquals("管理员", result.getRealName());
        verify(userMapper, times(1)).selectById(1L);
        System.out.println("✓ UserService.getById() - 测试通过");
    }

    @Test
    @Order(7)
    public void testGetCurrentUser() {
        // 测试获取当前用户
        when(userMapper.selectById(1L)).thenReturn(adminUser);
        when(roleMapper.selectById(1L)).thenReturn(adminRole);
        when(deptMapper.selectById(100L)).thenReturn(adminDept);

        UserDTO result = userService.getCurrentUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userMapper, times(1)).selectById(1L);
        System.out.println("✓ UserService.getCurrentUser() - 测试通过");
    }

    @Test
    @Order(8)
    public void testCreate() {
        // 测试创建用户
        when(userMapper.insert(any(UserDO.class))).thenReturn(1);

        UserDTO dto = new UserDTO();
        dto.setUsername("test_user");
        dto.setRealName("测试用户");
        dto.setDeptId(100L);

        userService.create(dto);

        verify(userMapper, times(1)).insert(any(UserDO.class));
        // 验证密码被设置为 MD5 加密的 123456
        verify(userMapper).insert(argThat(user ->
            "e10adc3949ba59abbe56e057f20f883e".equals(user.getPassword())
        ));
        System.out.println("✓ UserService.create() - 测试通过");
    }

    @Test
    @Order(9)
    public void testUpdate() {
        // 测试更新用户
        when(userMapper.selectById(1L)).thenReturn(adminUser);
        when(userMapper.updateById(any(UserDO.class))).thenReturn(1);

        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setRealName("修改后的管理员");

        userService.update(dto);

        verify(userMapper, times(1)).selectById(1L);
        verify(userMapper, times(1)).updateById(any(UserDO.class));
        System.out.println("✓ UserService.update() - 测试通过");
    }

    @Test
    @Order(10)
    public void testDelete() {
        // 测试删除用户
        when(userMapper.deleteById(1L)).thenReturn(1);

        userService.delete(1L);

        verify(userMapper, times(1)).deleteById(1L);
        System.out.println("✓ UserService.delete() - 测试通过");
    }
}
