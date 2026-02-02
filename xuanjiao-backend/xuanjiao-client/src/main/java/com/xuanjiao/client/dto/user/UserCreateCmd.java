package com.xuanjiao.client.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建用户命令对象
 */
@Data
public class UserCreateCmd {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    private String email;

    private String phone;

    private Long deptId;

    private Long roleId;
}
