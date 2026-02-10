package com.xuanjiao.client.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 用户登录命令
 *
 * <p>封装用户登录所需的凭据信息，用于身份验证。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class LoginCmd {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
