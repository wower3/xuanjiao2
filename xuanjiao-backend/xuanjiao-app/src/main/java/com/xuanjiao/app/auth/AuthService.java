package com.xuanjiao.app.auth;

import com.xuanjiao.client.LoginCmd;
import com.xuanjiao.client.LoginResultDTO;

/**
 * 认证服务接口
 *
 * <p>提供用户登录、登出等认证功能。使用JWT令牌进行身份验证。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>用户登录验证（用户名+密码）</li>
 *   <li>JWT令牌生成</li>
 *   <li>用户登出（令牌失效）</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.auth.impl.AuthServiceImpl
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * <p>验证用户名和密码，成功后返回JWT令牌和用户信息。
     * 密码使用MD5加密存储。</p>
     *
     * @param cmd 登录参数，包含用户名和密码
     * @return 登录结果，包含JWT令牌和用户信息
     * @throws RuntimeException 如果用户名或密码错误，或用户已被禁用
     */
    LoginResultDTO login(LoginCmd cmd);

    /**
     * 用户登出
     *
     * <p>使当前令牌失效。当前实现为空操作，可扩展为将令牌加入黑名单。</p>
     *
     * @param token 用户Token
     */
    void logout(String token);
}
