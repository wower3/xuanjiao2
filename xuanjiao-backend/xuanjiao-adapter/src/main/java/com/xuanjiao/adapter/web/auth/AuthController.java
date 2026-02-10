package com.xuanjiao.adapter.web.auth;

import com.xuanjiao.app.auth.AuthService;
import com.xuanjiao.client.dto.LoginCmd;
import com.xuanjiao.client.dto.LoginResultDTO;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.auth.LogoutCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 认证管理控制器
 *
 * <p>提供用户登录、登出、刷新令牌等认证相关功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>用户登录：用户名密码登录，返回 JWT 令牌</li>
 *   <li>用户登出：注销当前会话</li>
 *   <li>刷新令牌：使用刷新令牌获取新的访问令牌</li>
 *   <li>获取当前用户：获取当前登录用户信息</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * 认证服务
     *
     * <p>处理用户登录、登出、令牌管理等认证相关业务逻辑。</p>
     */
    @Resource
    private AuthService authService;

    /**
     * 用户登录
     *
     * <p>使用用户名和密码进行登录验证，验证成功后返回 JWT 令牌和用户信息。
     * 令牌需要在后续请求中通过 Authorization 请求头携带。</p>
     *
     * @param cmd 登录命令，包含用户名和密码
     * @return 登录结果，包含 JWT 令牌和用户基本信息
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginResultDTO> login(@Valid @RequestBody LoginCmd cmd) {
        return Result.success(authService.login(cmd));
    }

    /**
     * 用户登出
     *
     * <p>注销当前用户的会话，使令牌失效。登出后需要重新登录才能访问系统。</p>
     *
     * @param cmd 登出命令，包含要失效的令牌
     * @return 操作结果
     */
    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@Valid @RequestBody LogoutCmd cmd) {
        authService.logout(cmd.getToken());
        return Result.success();
    }
}
