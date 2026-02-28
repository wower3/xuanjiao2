package com.xuanjiao.client.role;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取角色详情查询对象
 *
 * <p>用于根据角色ID获取角色的详细信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class RoleGetDetailQry {

    /**
     * 角色ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;
}
