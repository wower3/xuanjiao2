package com.xuanjiao.adapter.web;

import com.xuanjiao.app.service.UserService;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.UserDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @ApiOperation("获取当前用户")
    @GetMapping("/current")
    public Result<UserDTO> getCurrentUser(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getCurrentUser(userId));
    }

    @ApiOperation("用户列表")
    @GetMapping("/list")
    public Result<List<UserDTO>> list() {
        return Result.success(userService.list());
    }

    @ApiOperation("新增用户")
    @PostMapping
    public Result<Void> create(@RequestBody UserDTO userDTO) {
        userService.create(userDTO);
        return Result.success();
    }

    @ApiOperation("更新用户")
    @PutMapping
    public Result<Void> update(@RequestBody UserDTO userDTO) {
        userService.update(userDTO);
        return Result.success();
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
