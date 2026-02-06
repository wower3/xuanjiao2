package com.xuanjiao.app.menu;

import com.xuanjiao.client.dto.MenuDTO;
import com.xuanjiao.client.dto.MenuCmd;

import java.util.List;

/**
 * 菜单服务接口
 * <p>提供菜单的查询、管理、权限分配等功能</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.menu.impl.MenuServiceImpl
 */
public interface MenuService {

    /**
     * 获取菜单树形结构
     *
     * @return 菜单DTO树形列表
     */
    List<MenuDTO> getTree();

    /**
     * 根据ID获取菜单
     *
     * @param id 菜单ID
     * @return 菜单DTO
     */
    MenuDTO getById(Long id);

    /**
     * 保存菜单
     *
     * @param cmd 菜单参数
     */
    void save(MenuCmd cmd);

    /**
     * 更新菜单
     *
     * @param cmd 菜单参数
     */
    void update(MenuCmd cmd);

    /**
     * 删除菜单
     *
     * @param id 菜单ID
     */
    void delete(Long id);

    /**
     * 为角色分配菜单
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     */
    void assignMenusToRole(Long roleId, List<Long> menuIds);

    /**
     * 根据角色ID获取菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);

    /**
     * 根据用户ID获取菜单列表
     *
     * @param userId 用户ID
     * @return 菜单DTO列表
     */
    List<MenuDTO> getMenusByUserId(Long userId);
}
