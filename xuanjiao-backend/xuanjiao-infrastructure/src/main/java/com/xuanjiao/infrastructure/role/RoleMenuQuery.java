package com.xuanjiao.infrastructure.role;

import lombok.Data;

/**
 * 角色-菜单关联查询条件对象
 *
 * <p>用于查询角色与菜单的关联关系。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class RoleMenuQuery {

    /**
     * 关联ID
     */
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;
}
