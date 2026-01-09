package com.xuanjiao.adapter.web;

import com.xuanjiao.app.service.RoleService;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.RoleDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@Api(tags = "角色管理")
@RestController
@RequestMapping("/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    @ApiOperation("角色列表")
    @GetMapping("/list")
    public Result<List<RoleDTO>> list() {
        return Result.success(roleService.list());
    }

    @ApiOperation("新增角色")
    @PostMapping
    public Result<Void> create(@RequestBody RoleDTO dto) {
        roleService.create(dto);
        return Result.success();
    }

    @ApiOperation("更新角色")
    @PutMapping
    public Result<Void> update(@RequestBody RoleDTO dto) {
        roleService.update(dto);
        return Result.success();
    }

    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }
}
