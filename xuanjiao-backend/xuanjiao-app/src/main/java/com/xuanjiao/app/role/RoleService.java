package com.xuanjiao.app.role;

import com.xuanjiao.client.RoleDTO;
import java.util.List;

/**
 * 角色服务接口
 *
 * <p>提供角色的查询、管理、权限分配等功能。角色用于控制用户的菜单访问权限和数据权限。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>角色CRUD操作</li>
 *   <li>菜单权限分配</li>
 *   <li>角色-菜单关联查询</li>
 * </ul>
 *
 * <p>预设角色：</p>
 * <ul>
 *   <li>系统管理员（ROLE_ID=1）：拥有所有权限</li>
 *   <li>总消保管理岗：可查看所有数据</li>
 *   <li>分消保管理岗：只能查看所属分部数据</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.role.impl.RoleServiceImpl
 */
public interface RoleService {

    /**
     * 获取所有角色列表
     *
     * <p>返回系统中所有角色的列表。</p>
     *
     * @return 角色DTO列表
     */
    List<RoleDTO> list();

    /**
     * 根据ID获取角色
     *
     * <p>返回指定角色的详细信息。</p>
     *
     * @param id 角色ID
     * @return 角色DTO，不存在返回null
     */
    RoleDTO getById(Long id);

    /**
     * 创建角色
     *
     * <p>创建新角色。角色名称必须唯一。</p>
     *
     * @param dto 角色DTO，包含名称、描述、类型等信息
     * @throws RuntimeException 如果角色名称已存在
     */
    void create(RoleDTO dto);

    /**
     * 更新角色
     *
     * <p>更新已有角色的信息。可更新名称、描述、类型、状态等。</p>
     *
     * @param dto 角色DTO
     * @throws RuntimeException 如果角色不存在或名称冲突
     */
    void update(RoleDTO dto);

    /**
     * 删除角色
     *
     * <p>删除指定角色。如果角色已分配给用户，需要先解除关联。</p>
     *
     * @param id 角色ID
     * @throws RuntimeException 如果角色已分配给用户
     */
    void delete(Long id);

    /**
     * 为角色分配菜单权限
     *
     * <p>设置角色可访问的菜单列表。分配后会删除原有权限，重新建立关联。</p>
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @throws RuntimeException 如果角色不存在
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 根据角色ID获取菜单ID列表
     *
     * <p>返回指定角色可访问的所有菜单ID。</p>
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);
}
