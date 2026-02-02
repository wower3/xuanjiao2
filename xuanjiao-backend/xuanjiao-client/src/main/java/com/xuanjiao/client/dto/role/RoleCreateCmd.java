package com.xuanjiao.client.dto.role;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建角色命令对象
 */
@Data
public class RoleCreateCmd {

    @NotBlank(message = "角色名称不能为空")
    private String name;

    private String description;

    private Integer roleType;
}
