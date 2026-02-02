package com.xuanjiao.client.dto.menu;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取角色菜单查询对象
 */
@Data
public class MenuGetRoleMenusQry {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
