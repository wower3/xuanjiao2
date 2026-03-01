package com.xuanjiao.integration;

import com.xuanjiao.infrastructure.menu.MenuMapper;
import com.xuanjiao.infrastructure.menu.MenuQuery;
import com.xuanjiao.infrastructure.dataobject.MenuDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MenuMapper 集成测试
 * 验证 MenuMapper 重构后与数据库交互正确
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MenuMapperIntegrationTest {

    @Autowired
    private MenuMapper menuMapper;

    @Test
    @Order(1)
    void testSelectById() {
        MenuDO result = menuMapper.selectById(1L);
        if (result != null) {
            assertNotNull(result.getId());
            assertEquals(0, result.getDeleted());
            System.out.println("✓ Menu selectById: " + result.getName());
        } else {
            System.out.println("⚠ Menu selectById: No records found in database");
        }
    }

    @Test
    @Order(2)
    void testSelectList_EmptyQuery() {
        MenuQuery query = new MenuQuery();
        List<MenuDO> list = menuMapper.selectList(query);
        assertNotNull(list);
        System.out.println("✓ Menu selectList (empty): " + list.size() + " records");
    }

    @Test
    @Order(3)
    void testSelectList_WithType() {
        MenuQuery query = new MenuQuery();
        query.setType("MENU");
        query.setStatus(1);
        query.setOrderByField("sort");
        query.setOrderByDirection("ASC");
        List<MenuDO> list = menuMapper.selectList(query);
        assertNotNull(list);
        for (MenuDO item : list) {
            assertEquals("MENU", item.getType());
            assertEquals(1, item.getStatus());
        }
        System.out.println("✓ Menu selectList (type=MENU, status=1): " + list.size() + " records");
    }

    @Test
    @Order(4)
    void testSelectList_WithParentId() {
        MenuQuery query = new MenuQuery();
        query.setParentId(0L);
        List<MenuDO> list = menuMapper.selectList(query);
        assertNotNull(list);
        for (MenuDO item : list) {
            assertEquals(0L, item.getParentId());
        }
        System.out.println("✓ Menu selectList (parentId=0): " + list.size() + " root menus");
    }

    @Test
    @Order(5)
    void testSelectCount() {
        MenuQuery query = new MenuQuery();
        Long count = menuMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ Menu selectCount: " + count + " records");
    }

    @Test
    @Order(6)
    void testSelectCount_WithType() {
        MenuQuery query = new MenuQuery();
        query.setType("MENU");
        query.setStatus(1);
        Long count = menuMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ Menu selectCount (type=MENU, status=1): " + count + " records");
    }

    @Test
    @Order(7)
    void testSelectMenuIdsByRoleId() {
        List<Long> menuIds = menuMapper.selectMenuIdsByRoleId(1L);
        assertNotNull(menuIds);
        System.out.println("✓ Menu selectMenuIdsByRoleId (roleId=1): " + menuIds.size() + " menu IDs");
    }

    @Test
    @Order(8)
    void testSelectMenusByUserId() {
        List<MenuDO> menus = menuMapper.selectMenusByUserId(1L);
        assertNotNull(menus);
        System.out.println("✓ Menu selectMenusByUserId (userId=1): " + menus.size() + " menus");
    }
}
