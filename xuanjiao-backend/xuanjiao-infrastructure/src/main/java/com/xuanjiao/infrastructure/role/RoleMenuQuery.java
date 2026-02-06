package com.xuanjiao.infrastructure.role;

import lombok.Data;

/**
 * RoleMenu查询条件对象
 * 用于查询角色菜单关联关系
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
