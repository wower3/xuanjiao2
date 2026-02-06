package com.xuanjiao.app.role;

import com.xuanjiao.client.dto.RoleDTO;
import java.util.List;

/**
 * 角色服务接口
 * <p>提供角色的查询、管理、权限分配等功能</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.role.impl.RoleServiceImpl
 */
public interface RoleService {

    /**
     * 获取所有角色列表
     *
     * @return 角色DTO列表
     */
    List<RoleDTO> list();

    /**
     * 根据ID获取角色
     *
     * @param id 角色ID
     * @return 角色DTO
     */
    RoleDTO getById(Long id);

    /**
     * 创建角色
     *
     * @param dto 角色DTO
     */
    void create(RoleDTO dto);

    /**
     * 更新角色
     *
     * @param dto 角色DTO
     */
    void update(RoleDTO dto);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void delete(Long id);

    /**
     * 为角色分配菜单权限
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 根据角色ID获取菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);
}
