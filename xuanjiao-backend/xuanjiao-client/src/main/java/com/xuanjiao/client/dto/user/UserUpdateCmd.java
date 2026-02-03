package com.xuanjiao.client.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 更新用户命令对象
 */
@Data
public class UserUpdateCmd {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String realName;

    private String email;

    private String phone;

    private Long deptId;

    private Long roleId;
}
