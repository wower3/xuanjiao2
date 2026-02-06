package com.xuanjiao.app.auth;

import com.xuanjiao.client.dto.LoginCmd;
import com.xuanjiao.client.dto.LoginResultDTO;

/**
 * 认证服务接口
 * <p>提供用户登录、登出等功能</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.auth.impl.AuthServiceImpl
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param cmd 登录参数
     * @return 登录结果
     */
    LoginResultDTO login(LoginCmd cmd);

    /**
     * 用户登出
     *
     * @param token 用户Token
     */
    void logout(String token);
}
