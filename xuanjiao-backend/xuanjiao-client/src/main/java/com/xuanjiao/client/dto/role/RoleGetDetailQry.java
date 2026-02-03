package com.xuanjiao.client.dto.role;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取角色详情查询对象
 */
@Data
public class RoleGetDetailQry {

    @NotNull(message = "ID不能为空")
    private Long id;
}
