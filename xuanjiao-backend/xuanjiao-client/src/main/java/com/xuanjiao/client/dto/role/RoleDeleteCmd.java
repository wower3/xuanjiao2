package com.xuanjiao.client.dto.role;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除角色命令对象
 */
@Data
public class RoleDeleteCmd {

    @NotNull(message = "ID不能为空")
    private Long id;
}
