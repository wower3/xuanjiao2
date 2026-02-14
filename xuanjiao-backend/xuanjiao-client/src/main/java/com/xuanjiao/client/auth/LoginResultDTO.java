package com.xuanjiao.client.auth;

import lombok.Data;

import com.xuanjiao.client.user.UserDTO;

/**
 * 登录结果数据传输对象
 *
 * <p>封装用户登录成功后返回的信息，包括JWT令牌和用户基本信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class LoginResultDTO {

    /**
     * JWT认证令牌
     */
    private String token;

    /**
     * 登录用户信息
     */
    private UserDTO user;
}
