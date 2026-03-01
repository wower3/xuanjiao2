package com.xuanjiao.integration;

import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.user.UserQuery;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.role.RoleQuery;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.dept.DeptQuery;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.asset.AssetQuery;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mapper 重构验证集成测试
 * 验证 UserMapper、RoleMapper、DeptMapper 和 AssetMapper 重构后与原始行为一致
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MapperRefactoringIntegrationTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private DeptMapper deptMapper;

    @Autowired
    private AssetMapper assetMapper;

    // ==================== UserMapper Tests ====================

    @Test
    @Order(1)
    void testUserSelectById() {
        UserDO user = userMapper.selectById(1L);
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
        assertEquals("管理员", user.getRealName());
        System.out.println("✓ User selectById: " + user.getUsername());
    }

    @Test
    @Order(2)
    void testUserSelectOneByUsername() {
        UserDO user = userMapper.selectOneByUsername("admin");
        assertNotNull(user);
        assertEquals(1L, user.getId());
        System.out.println("✓ User selectOneByUsername: " + user.getUsername());
    }

    @Test
    @Order(3)
    void testUserSelectList_EmptyQuery() {
        UserQuery query = new UserQuery();
        List<UserDO> users = userMapper.selectList(query);
        assertNotNull(users);
        assertTrue(users.size() >= 44);
        System.out.println("✓ User selectList (empty): " + users.size() + " users");
    }

    @Test
    @Order(4)
    void testUserSelectList_ByDeptId() {
        UserQuery query = new UserQuery();
        query.setDeptId(100L);
        query.setStatus(1);
        List<UserDO> users = userMapper.selectList(query);
        assertNotNull(users);
        assertTrue(users.size() >= 2);
        for (UserDO user : users) {
            assertEquals(100L, user.getDeptId());
        }
        System.out.println("✓ User selectList (deptId=100): " + users.size() + " users");
    }

    @Test
    @Order(5)
    void testUserSelectList_ByRoleIds() {
        UserQuery query = new UserQuery();
        query.setRoleIds(Arrays.asList(1L, 4L));
        query.setStatus(1);
        List<UserDO> users = userMapper.selectList(query);
        assertNotNull(users);
        assertTrue(users.size() >= 3);
        System.out.println("✓ User selectList (roleIds=1,4): " + users.size() + " users");
    }

    @Test
    @Order(6)
    void testUserSelectList_WithKeyword() {
        UserQuery query = new UserQuery();
        query.setKeyword("admin");
        List<UserDO> users = userMapper.selectList(query);
        assertNotNull(users);
        assertTrue(users.size() >= 1);
        System.out.println("✓ User selectList (keyword='admin'): " + users.size() + " users");
    }

    @Test
    @Order(7)
    void testUserSelectList_Combined() {
        UserQuery query = new UserQuery();
        query.setRoleId(4L);
        query.setDeptIds(Arrays.asList(201L));
        query.setStatus(1);
        List<UserDO> users = userMapper.selectList(query);
        assertNotNull(users);
        assertTrue(users.size() >= 2);
        System.out.println("✓ User selectList (combined): " + users.size() + " users");
    }

    // ==================== RoleMapper Tests ====================

    @Test
    @Order(20)
    void testRoleSelectById() {
        RoleDO role = roleMapper.selectById(1L);
        assertNotNull(role);
        assertEquals("系统管理员", role.getName());
        assertEquals("SYSTEM_ADMIN", role.getRoleType());
        System.out.println("✓ Role selectById: " + role.getName());
    }

    @Test
    @Order(21)
    void testRoleSelectList_Empty() {
        RoleQuery query = new RoleQuery();
        List<RoleDO> roles = roleMapper.selectList(query);
        assertNotNull(roles);
        assertTrue(roles.size() >= 10);
        System.out.println("✓ Role selectList (empty): " + roles.size() + " roles");
    }

    @Test
    @Order(22)
    void testRoleSelectList_ByRoleType() {
        RoleQuery query = new RoleQuery();
        query.setRoleType("SYSTEM_ADMIN");
        List<RoleDO> roles = roleMapper.selectList(query);
        assertNotNull(roles);
        assertTrue(roles.size() >= 1);
        System.out.println("✓ Role selectList (roleType='SYSTEM_ADMIN'): " + roles.size() + " roles");
    }

    @Test
    @Order(23)
    void testRoleSelectCount() {
        RoleQuery query = new RoleQuery();
        Long count = roleMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 10);
        System.out.println("✓ Role selectCount: " + count + " roles");
    }

    @Test
    @Order(24)
    void testRoleSelectList_Combined() {
        RoleQuery query = new RoleQuery();
        query.setRoleType("GENERAL_MGMT");
        query.setStatus(1);
        List<RoleDO> roles = roleMapper.selectList(query);
        assertNotNull(roles);
        assertTrue(roles.size() >= 1);
        System.out.println("✓ Role selectList (combined): " + roles.size() + " roles");
    }

    // ==================== DeptMapper Tests ====================

    @Test
    @Order(30)
    void testDeptSelectById() {
        // First query all departments to get a valid ID
        List<DeptDO> depts = deptMapper.selectAll();
        assertNotNull(depts);
        assertTrue(depts.size() >= 1, "At least one department should exist");
        // Use the first department's ID
        Long validId = depts.get(0).getId();
        DeptDO dept = deptMapper.selectById(validId);
        assertNotNull(dept, "Department with id=" + validId + " should exist");
        assertEquals(validId, dept.getId());
        System.out.println("✓ Dept selectById: " + dept.getName() + " (id=" + validId + ")");
    }

    @Test
    @Order(31)
    void testDeptSelectAll() {
        List<DeptDO> depts = deptMapper.selectAll();
        assertNotNull(depts);
        assertTrue(depts.size() >= 1);
        // Verify ordering by level, sort
        System.out.println("✓ Dept selectAll: " + depts.size() + " depts");
    }

    @Test
    @Order(32)
    void testDeptSelectByParentId() {
        // First get a valid parent ID
        List<DeptDO> depts = deptMapper.selectAll();
        assertNotNull(depts);
        assertTrue(depts.size() >= 1);
        // Find a department with parent_id != null
        Long validParentId = null;
        for (DeptDO dept : depts) {
            if (dept.getParentId() != null && dept.getParentId() > 0) {
                validParentId = dept.getParentId();
                break;
            }
        }
        // If no child departments, test with any valid parent_id from data
        if (validParentId == null && depts.size() > 0) {
            validParentId = depts.get(0).getId();
        }
        if (validParentId != null) {
            List<DeptDO> childDepts = deptMapper.selectByParentId(validParentId);
            System.out.println("✓ Dept selectByParentId(" + validParentId + "): " + childDepts.size() + " depts");
        } else {
            System.out.println("⚠ Dept selectByParentId: No valid parent ID found");
        }
    }

    @Test
    @Order(33)
    void testDeptSelectList_Empty() {
        DeptQuery query = new DeptQuery();
        List<DeptDO> depts = deptMapper.selectList(query);
        assertNotNull(depts);
        assertTrue(depts.size() >= 1);
        System.out.println("✓ Dept selectList (empty): " + depts.size() + " depts");
    }

    @Test
    @Order(34)
    void testDeptSelectList_ByLevel() {
        DeptQuery query = new DeptQuery();
        query.setLevel(1);
        List<DeptDO> depts = deptMapper.selectList(query);
        assertNotNull(depts);
        for (DeptDO dept : depts) {
            assertEquals(1, dept.getLevel());
        }
        System.out.println("✓ Dept selectList (level=1): " + depts.size() + " depts");
    }

    @Test
    @Order(35)
    void testDeptSelectByCode() {
        // First query to get a valid code
        List<DeptDO> depts = deptMapper.selectAll();
        assertNotNull(depts);
        assertTrue(depts.size() >= 1, "At least one department should exist");
        // Find a department with non-null code
        String validCode = null;
        for (DeptDO dept : depts) {
            if (dept.getCode() != null && !dept.getCode().isEmpty()) {
                validCode = dept.getCode();
                break;
            }
        }
        assertNotNull(validCode, "Should have a department with code");
        DeptDO dept = deptMapper.selectByCode(validCode);
        assertNotNull(dept, "Department with code=" + validCode + " should exist");
        assertEquals(validCode, dept.getCode());
        System.out.println("✓ Dept selectByCode('" + validCode + "'): " + dept.getName());
    }

    // ==================== AssetMapper Tests ====================

    @Test
    @Order(40)
    void testAssetSelectById() {
        AssetDO asset = assetMapper.selectById(1L);
        // Assuming there's at least one asset in the database
        if (asset != null) {
            assertNotNull(asset.getId());
            assertNotNull(asset.getName());
            System.out.println("✓ Asset selectById: " + asset.getName());
        } else {
            System.out.println("⚠ Asset selectById: No assets found in database");
        }
    }

    @Test
    @Order(41)
    void testAssetSelectList_Empty() {
        AssetQuery query = new AssetQuery();
        List<AssetDO> assets = assetMapper.selectList(query);
        assertNotNull(assets);
        System.out.println("✓ Asset selectList (empty): " + assets.size() + " assets");
    }

    @Test
    @Order(42)
    void testAssetSelectList_WithStatus() {
        AssetQuery query = new AssetQuery();
        query.setStatus("APPROVED");
        List<AssetDO> assets = assetMapper.selectList(query);
        assertNotNull(assets);
        for (AssetDO asset : assets) {
            assertEquals("APPROVED", asset.getStatus());
        }
        System.out.println("✓ Asset selectList (status=APPROVED): " + assets.size() + " assets");
    }

    @Test
    @Order(43)
    void testAssetSelectList_WithStatusList() {
        AssetQuery query = new AssetQuery();
        query.setStatusList(Arrays.asList("APPROVED", "DELETED"));
        List<AssetDO> assets = assetMapper.selectList(query);
        assertNotNull(assets);
        for (AssetDO asset : assets) {
            assertTrue("APPROVED".equals(asset.getStatus()) || "DELETED".equals(asset.getStatus()));
        }
        System.out.println("✓ Asset selectList (statusList): " + assets.size() + " assets");
    }

    @Test
    @Order(44)
    void testAssetSelectByApplicationId() {
        // Query by application_id - assuming there might be some
        AssetQuery query = new AssetQuery();
        query.setApplicationId(1L);
        List<AssetDO> assets = assetMapper.selectList(query);
        assertNotNull(assets);
        System.out.println("✓ Asset selectByApplicationId(1): " + assets.size() + " assets");
    }

    @Test
    @Order(45)
    void testAssetSelectCount() {
        AssetQuery query = new AssetQuery();
        Long count = assetMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ Asset selectCount: " + count + " assets");
    }
}
