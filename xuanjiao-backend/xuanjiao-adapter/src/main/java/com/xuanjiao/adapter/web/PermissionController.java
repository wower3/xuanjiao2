package com.xuanjiao.adapter.web;

import com.xuanjiao.app.service.PermissionService;
import com.xuanjiao.client.dto.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "权限管理")
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Resource
    private PermissionService permissionService;

    @ApiOperation("获取当前用户权限")
    @GetMapping("/current")
    public Result<List<Map<String, Object>>> getCurrentPermissions(
            @RequestAttribute("userId") Long userId) {
        return Result.success(permissionService.getUserPermissions(userId));
    }

    @ApiOperation("获取当前用户权限编码")
    @GetMapping("/codes")
    public Result<List<String>> getCurrentPermissionCodes(
            @RequestAttribute("userId") Long userId) {
        return Result.success(permissionService.getUserPermissionCodes(userId));
    }
}
