package com.xuanjiao.app.menu;

import com.xuanjiao.app.menu.impl.MenuServiceImpl;
import com.xuanjiao.client.MenuCmd;
import com.xuanjiao.client.MenuDTO;
import com.xuanjiao.infrastructure.dataobject.MenuDO;
import com.xuanjiao.infrastructure.menu.MenuMapper;
import com.xuanjiao.infrastructure.menu.MenuQuery;
import com.xuanjiao.infrastructure.role.RoleMenuMapper;
import com.xuanjiao.infrastructure.role.RoleMenuQuery;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MenuServiceImpl 单元测试
 * 验证 MenuMapper 重构后功能正确
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MenuServiceImplTest {

    @Mock
    private MenuMapper menuMapper;

    @Mock
    private RoleMenuMapper roleMenuMapper;

    @InjectMocks
    private MenuServiceImpl menuService;

    private MenuDO testMenu;
    private MenuDO testSubMenu;

    @BeforeEach
    public void setUp() {
        // Create test menu data
        testMenu = new MenuDO();
        testMenu.setId(1L);
        testMenu.setParentId(0L);
        testMenu.setName("系统管理");
        testMenu.setType("MENU");
        testMenu.setPath("/system");
        testMenu.setComponent("system/index");
        testMenu.setIcon("system");
        testMenu.setSort(1);
        testMenu.setStatus(1);
        testMenu.setDeleted(0);
        testMenu.setCreateTime(LocalDateTime.now());
        testMenu.setUpdateTime(LocalDateTime.now());

        testSubMenu = new MenuDO();
        testSubMenu.setId(2L);
        testSubMenu.setParentId(1L);
        testSubMenu.setName("用户管理");
        testSubMenu.setType("MENU");
        testSubMenu.setPath("/system/user");
        testSubMenu.setComponent("system/user/index");
        testSubMenu.setIcon("user");
        testSubMenu.setSort(1);
        testSubMenu.setStatus(1);
        testSubMenu.setDeleted(0);
        testSubMenu.setCreateTime(LocalDateTime.now());
        testSubMenu.setUpdateTime(LocalDateTime.now());
    }

    // ==================== MenuMapper Call Site Tests ====================

    @Test
    @Order(1)
    public void testGetTree() {
        // 测试获取菜单树
        // This tests: menuMapper.selectList with type=MENU, status=1, orderBy sort ASC

        when(menuMapper.selectList(any(MenuQuery.class)))
                .thenReturn(Arrays.asList(testMenu, testSubMenu));

        List<MenuDTO> result = menuService.getTree();

        assertNotNull(result);
        assertEquals(1, result.size()); // Only root menu (parentId=0)
        assertEquals("系统管理", result.get(0).getName());
        assertEquals(1, result.get(0).getChildren().size()); // One sub-menu
        // 验证 menuMapper.selectList 被调用
        verify(menuMapper).selectList(argThat(query ->
                query != null && "MENU".equals(query.getType()) &&
                query.getStatus() != null && query.getStatus() == 1 &&
                "sort".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ));
        System.out.println("✓ MenuService.getTree() - menuMapper.selectList 测试通过");
    }

    @Test
    @Order(2)
    public void testGetById() {
        // 测试根据ID获取菜单
        // This tests: menuMapper.selectById

        when(menuMapper.selectById(1L)).thenReturn(testMenu);

        MenuDTO result = menuService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("系统管理", result.getName());
        // 验证 menuMapper.selectById 被调用
        verify(menuMapper).selectById(1L);
        System.out.println("✓ MenuService.getById() - menuMapper.selectById 测试通过");
    }

    @Test
    @Order(3)
    public void testSave() {
        // 测试保存菜单
        // This tests: menuMapper.insert

        doAnswer(invocation -> {
            MenuDO menu = invocation.getArgument(0);
            menu.setId(1L);
            return null;
        }).when(menuMapper).insert(any(MenuDO.class));

        MenuCmd cmd = new MenuCmd();
        cmd.setName("测试菜单");
        cmd.setType("MENU");
        cmd.setPath("/test");
        cmd.setParentId(0L);
        cmd.setSort(1);
        cmd.setStatus(1);

        menuService.save(cmd);

        // 验证 menuMapper.insert 被调用
        verify(menuMapper).insert(argThat(menu ->
                menu != null && "测试菜单".equals(menu.getName()) &&
                "MENU".equals(menu.getType())
        ));
        System.out.println("✓ MenuService.save() - menuMapper.insert 测试通过");
    }

    @Test
    @Order(4)
    public void testUpdate() {
        // 测试更新菜单
        // This tests: menuMapper.updateById

        when(menuMapper.updateById(any(MenuDO.class))).thenReturn(1);

        MenuCmd cmd = new MenuCmd();
        cmd.setId(1L);
        cmd.setName("更新后的菜单");
        cmd.setType("MENU");
        cmd.setPath("/system");
        cmd.setParentId(0L);
        cmd.setSort(1);
        cmd.setStatus(1);

        menuService.update(cmd);

        // 验证 menuMapper.updateById 被调用
        verify(menuMapper).updateById(argThat(menu ->
                menu != null && menu.getId() == 1L &&
                "更新后的菜单".equals(menu.getName())
        ));
        System.out.println("✓ MenuService.update() - menuMapper.updateById 测试通过");
    }

    @Test
    @Order(5)
    public void testDelete() {
        // 测试删除菜单
        // This tests: menuMapper.deleteById, roleMenuMapper.delete

        when(menuMapper.deleteById(1L)).thenReturn(1);
        when(roleMenuMapper.delete(any(RoleMenuQuery.class))).thenReturn(1);

        menuService.delete(1L);

        // 验证 menuMapper.deleteById 被调用
        verify(menuMapper).deleteById(1L);
        // 验证 roleMenuMapper.delete 被调用
        verify(roleMenuMapper).delete(argThat(query ->
                query != null && query.getMenuId() == 1L
        ));
        System.out.println("✓ MenuService.delete() - menuMapper+roleMenuMapper 测试通过");
    }

    @Test
    @Order(6)
    public void testGetMenuIdsByRoleId() {
        // 测试根据角色ID获取菜单ID列表
        // This tests: menuMapper.selectMenuIdsByRoleId

        when(menuMapper.selectMenuIdsByRoleId(1L))
                .thenReturn(Arrays.asList(1L, 2L, 3L));

        List<Long> result = menuService.getMenuIdsByRoleId(1L);

        assertNotNull(result);
        assertEquals(3, result.size());
        // 验证 menuMapper.selectMenuIdsByRoleId 被调用
        verify(menuMapper).selectMenuIdsByRoleId(1L);
        System.out.println("✓ MenuService.getMenuIdsByRoleId() - menuMapper.selectMenuIdsByRoleId 测试通过");
    }

    @Test
    @Order(7)
    public void testGetMenusByUserId() {
        // 测试根据用户ID获取菜单列表
        // This tests: menuMapper.selectMenusByUserId, menuMapper.selectList

        when(menuMapper.selectMenusByUserId(1L))
                .thenReturn(Arrays.asList(testMenu));
        when(menuMapper.selectList(any(MenuQuery.class)))
                .thenReturn(Arrays.asList(testMenu, testSubMenu));

        List<MenuDTO> result = menuService.getMenusByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size()); // Only root menu
        assertEquals("系统管理", result.get(0).getName());
        // 验证两个 mapper 方法都被调用
        verify(menuMapper).selectMenusByUserId(1L);
        verify(menuMapper).selectList(argThat(query ->
                query != null && "MENU".equals(query.getType()) &&
                query.getStatus() != null && query.getStatus() == 1
        ));
        System.out.println("✓ MenuService.getMenusByUserId() - menuMapper 测试通过");
    }

    @Test
    @Order(8)
    public void testAssignMenusToRole() {
        // 测试为角色分配菜单
        // This tests: roleMenuMapper.deleteByRoleId, roleMenuMapper.insert

        when(roleMenuMapper.deleteByRoleId(1L)).thenReturn(0);
        when(roleMenuMapper.insert(any())).thenReturn(1);

        List<Long> menuIds = Arrays.asList(1L, 2L, 3L);
        menuService.assignMenusToRole(1L, menuIds);

        // 验证 roleMenuMapper 方法被调用
        verify(roleMenuMapper).deleteByRoleId(1L);
        verify(roleMenuMapper, times(3)).insert(any());
        System.out.println("✓ MenuService.assignMenusToRole() - roleMenuMapper 测试通过");
    }
}
