package com.xuanjiao.app.menu;

import com.xuanjiao.client.dto.menu.dto.MenuDTO;
import com.xuanjiao.client.dto.menu.MenuCmd;

import java.util.List;

/**
 * 菜单服务接口
 *
 * <p>提供菜单的查询、管理、权限分配等功能。菜单采用树形结构组织，
 * 支持无限层级嵌套。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>菜单CRUD操作</li>
 *   <li>菜单树形结构查询</li>
 *   <li>角色菜单权限分配</li>
 *   <li>用户菜单列表查询</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.menu.impl.MenuServiceImpl
 */
public interface MenuService {

    /**
     * 获取菜单树形结构
     *
     * <p>返回菜单树形结构，以顶级菜单为根节点，包含所有子菜单的嵌套结构。</p>
     *
     * @return 菜单DTO树形列表，每个节点的children字段包含子菜单
     */
    List<MenuDTO> getTree();

    /**
     * 根据ID获取菜单
     *
     * <p>返回指定菜单的详细信息。</p>
     *
     * @param id 菜单ID
     * @return 菜单DTO，不存在返回null
     */
    MenuDTO getById(Long id);

    /**
     * 保存菜单
     *
     * <p>创建新菜单。需要指定父菜单ID（顶级菜单传null或0）。</p>
     *
     * @param cmd 菜单参数，包含名称、父ID、路径、图标、排序等信息
     */
    void save(MenuCmd cmd);

    /**
     * 更新菜单
     *
     * <p>更新已有菜单的信息。可更新名称、路径、图标、排序、状态等。</p>
     *
     * @param cmd 菜单参数
     * @throws RuntimeException 如果菜单不存在
     */
    void update(MenuCmd cmd);

    /**
     * 删除菜单
     *
     * <p>删除指定菜单。如果菜单有子菜单，需要先删除子菜单。</p>
     *
     * @param id 菜单ID
     * @throws RuntimeException 如果菜单有子菜单
     */
    void delete(Long id);

    /**
     * 为角色分配菜单
     *
     * <p>设置角色可访问的菜单列表。分配后会删除原有权限，重新建立关联。</p>
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @throws RuntimeException 如果角色不存在
     */
    void assignMenusToRole(Long roleId, List<Long> menuIds);

    /**
     * 根据角色ID获取菜单ID列表
     *
     * <p>返回指定角色可访问的所有菜单ID。</p>
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);

    /**
     * 根据用户ID获取菜单列表
     *
     * <p>返回用户可访问的菜单列表。根据用户角色确定可访问的菜单范围。
     * 系统管理员（ROLE_ID=1）可访问所有菜单。</p>
     *
     * @param userId 用户ID
     * @return 菜单DTO列表，树形结构
     */
    List<MenuDTO> getMenusByUserId(Long userId);
}
