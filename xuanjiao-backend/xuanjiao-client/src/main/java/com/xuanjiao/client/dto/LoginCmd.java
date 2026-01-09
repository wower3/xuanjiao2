package com.xuanjiao.client.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginCmd {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
