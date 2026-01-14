package com.xuanjiao.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.app.service.MenuService;
import com.xuanjiao.client.dto.MenuCmd;
import com.xuanjiao.client.dto.MenuDTO;
import com.xuanjiao.infrastructure.dataobject.MenuDO;
import com.xuanjiao.infrastructure.dataobject.RoleMenuDO;
import com.xuanjiao.infrastructure.mapper.MenuMapper;
import com.xuanjiao.infrastructure.mapper.RoleMenuMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Override
    public List<MenuDTO> getTree() {
        LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuDO::getType, "MENU")
               .eq(MenuDO::getStatus, 1)
               .orderByAsc(MenuDO::getSort);
        List<MenuDO> all = menuMapper.selectList(wrapper);
        return buildTree(all, 0L);
    }

    @Override
    public MenuDTO getById(Long id) {
        MenuDO menuDO = menuMapper.selectById(id);
        return convert(menuDO);
    }

    @Override
    @Transactional
    public void save(MenuCmd cmd) {
        MenuDO menuDO = new MenuDO();
        BeanUtils.copyProperties(cmd, menuDO);
        menuMapper.insert(menuDO);
    }

    @Override
    @Transactional
    public void update(MenuCmd cmd) {
        MenuDO menuDO = new MenuDO();
        BeanUtils.copyProperties(cmd, menuDO);
        menuMapper.updateById(menuDO);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        menuMapper.deleteById(id);
        // 同时删除角色菜单关联
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenuDO>()
                .eq(RoleMenuDO::getMenuId, id));
    }

    @Override
    @Transactional
    public void assignMenusToRole(Long roleId, List<Long> menuIds) {
        // 先删除原有的关联
        roleMenuMapper.deleteByRoleId(roleId);
        // 添加新的关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                RoleMenuDO rm = new RoleMenuDO();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return menuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public List<MenuDTO> getMenusByUserId(Long userId) {
        List<MenuDO> menus = menuMapper.selectMenusByUserId(userId);
        List<MenuDO> menuList = menus.stream()
                .filter(m -> "MENU".equals(m.getType()))
                .collect(Collectors.toList());

        // 获取用户有权限的菜单ID集合
        List<Long> userMenuIds = menuList.stream()
                .map(MenuDO::getId)
                .collect(Collectors.toList());

        // 查询所有类型为MENU的菜单（用于构建完整的树）
        LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuDO::getType, "MENU")
               .eq(MenuDO::getStatus, 1)
               .orderByAsc(MenuDO::getSort);
        List<MenuDO> allMenus = menuMapper.selectList(wrapper);

        // 确定需要包含在树中的菜单：用户有权限的菜单 + 所有父级菜单
        Set<Long> menuIdsToInclude = new java.util.HashSet<>();
        for (MenuDO menu : allMenus) {
            if (userMenuIds.contains(menu.getId())) {
                // 用户有权限的菜单，包含它及其所有父级
                addWithParents(menu, menuIdsToInclude, allMenus);
            }
        }

        // 构建最终的菜单列表（只包含需要的菜单）
        List<MenuDO> filteredMenus = allMenus.stream()
                .filter(m -> menuIdsToInclude.contains(m.getId()))
                .collect(Collectors.toList());

        return buildTree(filteredMenus, 0L);
    }

    /**
     * 递归添加菜单及其所有父级ID
     */
    private void addWithParents(MenuDO menu, Set<Long> menuIds, List<MenuDO> allMenus) {
        menuIds.add(menu.getId());
        if (menu.getParentId() != null && menu.getParentId() != 0L) {
            for (MenuDO parent : allMenus) {
                if (parent.getId().equals(menu.getParentId())) {
                    addWithParents(parent, menuIds, allMenus);
                    break;
                }
            }
        }
    }

    private List<MenuDTO> buildTree(List<MenuDO> all, Long parentId) {
        List<MenuDTO> result = new ArrayList<>();
        for (MenuDO menu : all) {
            if (parentId.equals(menu.getParentId())) {
                MenuDTO dto = convert(menu);
                dto.setChildren(buildTree(all, menu.getId()));
                result.add(dto);
            }
        }
        return result;
    }

    private MenuDTO convert(MenuDO menuDO) {
        if (menuDO == null) return null;
        MenuDTO dto = new MenuDTO();
        BeanUtils.copyProperties(menuDO, dto);
        return dto;
    }
}
