package com.xuanjiao.client.role;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取角色菜单查询对象
 *
 * <p>用于查询指定角色关联的菜单ID列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class RoleGetRoleMenusQry {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
