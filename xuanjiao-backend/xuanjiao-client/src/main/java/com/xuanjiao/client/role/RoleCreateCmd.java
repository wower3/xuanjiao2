package com.xuanjiao.client.role;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建角色命令对象
 *
 * <p>封装创建角色所需的参数信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class RoleCreateCmd {

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String name;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色类型（1-管理员、2-审批人、3-普通用户）
     */
    private Integer roleType;
}
