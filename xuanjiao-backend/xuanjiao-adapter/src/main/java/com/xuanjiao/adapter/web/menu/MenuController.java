package com.xuanjiao.adapter.web.menu;

import com.xuanjiao.app.menu.MenuService;
import com.xuanjiao.client.dto.MenuCmd;
import com.xuanjiao.client.dto.MenuDTO;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.menu.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "菜单管理")
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Resource
    private MenuService menuService;

    @ApiOperation("获取菜单树")
    @PostMapping("/getTree")
    public Result<List<MenuDTO>> getTree(@Valid @RequestBody MenuGetTreeQry qry) {
        return Result.success(menuService.getTree());
    }

    @ApiOperation("获取当前用户菜单")
    @PostMapping("/getCurrent")
    public Result<List<MenuDTO>> getCurrentMenus(@RequestAttribute("userId") Long userId, @Valid @RequestBody MenuGetCurrentQry qry) {
        return Result.success(menuService.getMenusByUserId(userId));
    }

    @ApiOperation("获取菜单详情")
    @PostMapping("/getDetail")
    public Result<MenuDTO> getById(@Valid @RequestBody MenuGetDetailQry qry) {
        return Result.success(menuService.getById(qry.getId()));
    }

    @ApiOperation("新增菜单")
    @PostMapping("/create")
    public Result<Void> save(@Valid @RequestBody MenuCreateCmd cmd) {
        menuService.save(convertToMenuCmd(cmd));
        return Result.success();
    }

    @ApiOperation("更新菜单")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody MenuUpdateCmd cmd) {
        menuService.update(convertToMenuCmd(cmd));
        return Result.success();
    }

    @ApiOperation("删除菜单")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody MenuDeleteCmd cmd) {
        menuService.delete(cmd.getId());
        return Result.success();
    }

    @ApiOperation("分配角色菜单权限")
    @PostMapping("/assign")
    public Result<Void> assignMenus(@RequestParam Long roleId, @RequestBody List<Long> menuIds) {
        menuService.assignMenusToRole(roleId, menuIds);
        return Result.success();
    }

    @ApiOperation("获取角色菜单权限")
    @PostMapping("/getRoleMenus")
    public Result<List<Long>> getRoleMenus(@Valid @RequestBody MenuGetRoleMenusQry qry) {
        return Result.success(menuService.getMenuIdsByRoleId(qry.getRoleId()));
    }

    private MenuCmd convertToMenuCmd(MenuCreateCmd cmd) {
        MenuCmd menuCmd = new MenuCmd();
        menuCmd.setName(cmd.getName());
        menuCmd.setPath(cmd.getPath());
        menuCmd.setComponent(cmd.getComponent());
        menuCmd.setSort(cmd.getSort());
        menuCmd.setParentId(cmd.getParentId());
        menuCmd.setIcon(cmd.getIcon());
        menuCmd.setType(cmd.getMenuType() != null ? String.valueOf(cmd.getMenuType()) : null);
        return menuCmd;
    }

    private MenuCmd convertToMenuCmd(MenuUpdateCmd cmd) {
        MenuCmd menuCmd = new MenuCmd();
        menuCmd.setId(cmd.getId());
        menuCmd.setName(cmd.getName());
        menuCmd.setPath(cmd.getPath());
        menuCmd.setComponent(cmd.getComponent());
        menuCmd.setSort(cmd.getSort());
        menuCmd.setParentId(cmd.getParentId());
        menuCmd.setIcon(cmd.getIcon());
        menuCmd.setType(cmd.getMenuType() != null ? String.valueOf(cmd.getMenuType()) : null);
        return menuCmd;
    }
}
