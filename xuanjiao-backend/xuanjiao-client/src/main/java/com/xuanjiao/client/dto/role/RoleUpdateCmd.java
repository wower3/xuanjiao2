package com.xuanjiao.client.dto.role;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 更新角色命令对象
 */
@Data
public class RoleUpdateCmd {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    private String name;

    private String description;

    private Integer roleType;
}
