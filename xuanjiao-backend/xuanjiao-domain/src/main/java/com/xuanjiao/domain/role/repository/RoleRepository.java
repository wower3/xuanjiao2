package com.xuanjiao.domain.role.repository;

import com.xuanjiao.domain.role.entity.Role;
import java.util.List;

/**
 * 角色仓储接口
 *
 * <p>定义角色数据的持久化操作，包括角色的查询、保存、更新和删除。</p>
 * <p>角色关联菜单权限，用户通过角色获得权限。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface RoleRepository {

    /**
     * 根据ID查找角色
     *
     * @param id 角色ID
     * @return 角色实体，如果不存在返回 null
     */
    Role findById(Long id);

    /**
     * 查找所有角色
     *
     * @return 所有角色列表
     */
    List<Role> findAll();

    /**
     * 保存角色
     *
     * @param role 角色实体
     */
    void save(Role role);

    /**
     * 更新角色
     *
     * @param role 角色实体
     */
    void update(Role role);

    /**
     * 根据ID删除角色
     *
     * @param id 角色ID
     */
    void deleteById(Long id);
}
