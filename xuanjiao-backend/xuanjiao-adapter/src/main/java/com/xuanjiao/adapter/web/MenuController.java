package com.xuanjiao.adapter.web;

import com.xuanjiao.app.service.MenuService;
import com.xuanjiao.client.dto.MenuCmd;
import com.xuanjiao.client.dto.MenuDTO;
import com.xuanjiao.client.dto.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@Api(tags = "菜单管理")
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Resource
    private MenuService menuService;

    @ApiOperation("获取菜单树")
    @GetMapping("/tree")
    public Result<List<MenuDTO>> getTree() {
        return Result.success(menuService.getTree());
    }

    @ApiOperation("获取当前用户菜单")
    @GetMapping("/current")
    public Result<List<MenuDTO>> getCurrentMenus(@RequestAttribute("userId") Long userId) {
        return Result.success(menuService.getMenusByUserId(userId));
    }

    @ApiOperation("获取菜单详情")
    @GetMapping("/{id}")
    public Result<MenuDTO> getById(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @ApiOperation("新增菜单")
    @PostMapping
    public Result<Void> save(@RequestBody MenuCmd cmd) {
        menuService.save(cmd);
        return Result.success();
    }

    @ApiOperation("更新菜单")
    @PutMapping
    public Result<Void> update(@RequestBody MenuCmd cmd) {
        menuService.update(cmd);
        return Result.success();
    }

    @ApiOperation("删除菜单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }

    @ApiOperation("分配角色菜单权限")
    @PostMapping("/assign")
    public Result<Void> assignMenus(@RequestParam Long roleId, @RequestBody List<Long> menuIds) {
        menuService.assignMenusToRole(roleId, menuIds);
        return Result.success();
    }

    @ApiOperation("获取角色菜单权限")
    @GetMapping("/role/{roleId}")
    public Result<List<Long>> getRoleMenus(@PathVariable Long roleId) {
        return Result.success(menuService.getMenuIdsByRoleId(roleId));
    }
}
