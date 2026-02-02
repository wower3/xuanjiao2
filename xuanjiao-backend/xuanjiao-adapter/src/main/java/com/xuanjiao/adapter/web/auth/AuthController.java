package com.xuanjiao.adapter.web.auth;

import com.xuanjiao.app.auth.AuthService;
import com.xuanjiao.client.dto.*;
import com.xuanjiao.client.dto.auth.LogoutCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginResultDTO> login(@Valid @RequestBody LoginCmd cmd) {
        return Result.success(authService.login(cmd));
    }

    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@Valid @RequestBody LogoutCmd cmd) {
        authService.logout(cmd.getToken());
        return Result.success();
    }
}
