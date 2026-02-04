package com.xuanjiao.integration;

import com.xuanjiao.infrastructure.role.RoleMenuMapper;
import com.xuanjiao.infrastructure.role.RoleMenuQuery;
import com.xuanjiao.infrastructure.dataobject.RoleMenuDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RoleMenuMapper 集成测试
 * 验证 RoleMenuMapper 重构后与数据库交互正确
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleMenuMapperIntegrationTest {

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Test
    @Order(1)
    public void testSelectList_EmptyQuery() {
        RoleMenuQuery query = new RoleMenuQuery();
        java.util.List<RoleMenuDO> list = roleMenuMapper.selectList(query);
        assertNotNull(list);
        System.out.println("✓ RoleMenu selectList (empty): " + list.size() + " records");
    }

    @Test
    @Order(2)
    public void testSelectList_WithRoleId() {
        // Find a role with menus
        RoleMenuQuery query = new RoleMenuQuery();
        java.util.List<RoleMenuDO> allRoleMenus = roleMenuMapper.selectList(query);

        if (!allRoleMenus.isEmpty()) {
            Long roleId = allRoleMenus.get(0).getRoleId();
            RoleMenuQuery roleQuery = new RoleMenuQuery();
            roleQuery.setRoleId(roleId);
            java.util.List<RoleMenuDO> list = roleMenuMapper.selectList(roleQuery);
            assertNotNull(list);
            for (RoleMenuDO item : list) {
                assertEquals(roleId, item.getRoleId());
            }
            System.out.println("✓ RoleMenu selectList (roleId=" + roleId + "): " + list.size() + " records");
        } else {
            System.out.println("⚠ RoleMenu selectList (roleId): No records found in database");
        }
    }

    @Test
    @Order(3)
    public void testSelectList_WithMenuId() {
        // Find a menu with roles
        RoleMenuQuery query = new RoleMenuQuery();
        java.util.List<RoleMenuDO> allRoleMenus = roleMenuMapper.selectList(query);

        if (!allRoleMenus.isEmpty()) {
            Long menuId = allRoleMenus.get(0).getMenuId();
            RoleMenuQuery menuQuery = new RoleMenuQuery();
            menuQuery.setMenuId(menuId);
            java.util.List<RoleMenuDO> list = roleMenuMapper.selectList(menuQuery);
            assertNotNull(list);
            for (RoleMenuDO item : list) {
                assertEquals(menuId, item.getMenuId());
            }
            System.out.println("✓ RoleMenu selectList (menuId=" + menuId + "): " + list.size() + " records");
        } else {
            System.out.println("⚠ RoleMenu selectList (menuId): No records found in database");
        }
    }

    @Test
    @Order(4)
    public void testSelectCount() {
        RoleMenuQuery query = new RoleMenuQuery();
        Long count = roleMenuMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ RoleMenu selectCount: " + count + " records");
    }

    @Test
    @Order(5)
    public void testInsertAndDelete() {
        // Test insert
        RoleMenuDO newRoleMenu = new RoleMenuDO();
        newRoleMenu.setRoleId(999999L); // Use a non-existent role ID for testing
        newRoleMenu.setMenuId(999999L);   // Use a non-existent menu ID for testing

        int insertResult = roleMenuMapper.insert(newRoleMenu);
        assertTrue(insertResult > 0);
        assertNotNull(newRoleMenu.getId());
        System.out.println("✓ RoleMenu insert: 1 record inserted, id=" + newRoleMenu.getId());

        // Test delete by query
        RoleMenuQuery deleteQuery = new RoleMenuQuery();
        deleteQuery.setRoleId(999999L);
        int deleteResult = roleMenuMapper.delete(deleteQuery);
        assertTrue(deleteResult > 0);
        System.out.println("✓ RoleMenu delete: " + deleteResult + " record(s) deleted");
    }

    @Test
    @Order(6)
    public void testDeleteByRoleId() {
        // First insert a test record
        RoleMenuDO newRoleMenu = new RoleMenuDO();
        newRoleMenu.setRoleId(888888L);
        newRoleMenu.setMenuId(888888L);
        roleMenuMapper.insert(newRoleMenu);

        // Test deleteByRoleId
        int deleteResult = roleMenuMapper.deleteByRoleId(888888L);
        assertTrue(deleteResult > 0);
        System.out.println("✓ RoleMenu deleteByRoleId: " + deleteResult + " record(s) deleted");
    }
}
