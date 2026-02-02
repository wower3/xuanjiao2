package com.xuanjiao.adapter.web.role;

import com.xuanjiao.app.role.RoleService;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.RoleDTO;
import com.xuanjiao.client.dto.role.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "角色管理")
@RestController
@RequestMapping("/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    @ApiOperation("角色列表")
    @PostMapping("/getList")
    public Result<List<RoleDTO>> list(@Valid @RequestBody RoleGetListQry qry) {
        return Result.success(roleService.list());
    }

    @ApiOperation("获取角色详情")
    @PostMapping("/getDetail")
    public Result<RoleDTO> getById(@Valid @RequestBody RoleGetDetailQry qry) {
        return Result.success(roleService.getById(qry.getId()));
    }

    @ApiOperation("新增角色")
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody RoleCreateCmd cmd) {
        roleService.create(convertToDto(cmd));
        return Result.success();
    }

    @ApiOperation("更新角色")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody RoleUpdateCmd cmd) {
        roleService.update(convertToDto(cmd));
        return Result.success();
    }

    @ApiOperation("删除角色")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody RoleDeleteCmd cmd) {
        roleService.delete(cmd.getId());
        return Result.success();
    }

    @ApiOperation("分配角色菜单权限")
    @PostMapping("/{roleId}/menus")
    public Result<Void> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
        return Result.success();
    }

    @ApiOperation("获取角色菜单权限")
    @PostMapping("/getRoleMenus")
    public Result<List<Long>> getRoleMenus(@Valid @RequestBody RoleGetRoleMenusQry qry) {
        return Result.success(roleService.getMenuIdsByRoleId(qry.getRoleId()));
    }

    private RoleDTO convertToDto(RoleCreateCmd cmd) {
        RoleDTO dto = new RoleDTO();
        dto.setName(cmd.getName());
        dto.setDescription(cmd.getDescription());
        dto.setRoleType(cmd.getRoleType() != null ? String.valueOf(cmd.getRoleType()) : null);
        return dto;
    }

    private RoleDTO convertToDto(RoleUpdateCmd cmd) {
        RoleDTO dto = new RoleDTO();
        dto.setId(cmd.getId());
        dto.setName(cmd.getName());
        dto.setDescription(cmd.getDescription());
        dto.setRoleType(cmd.getRoleType() != null ? String.valueOf(cmd.getRoleType()) : null);
        return dto;
    }
}
