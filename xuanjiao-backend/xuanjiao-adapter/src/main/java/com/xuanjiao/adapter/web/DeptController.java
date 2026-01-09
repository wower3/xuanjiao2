package com.xuanjiao.adapter.web;

import com.xuanjiao.app.service.DeptService;
import com.xuanjiao.client.dto.DeptDTO;
import com.xuanjiao.client.dto.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@Api(tags = "部门管理")
@RestController
@RequestMapping("/dept")
public class DeptController {

    @Resource
    private DeptService deptService;

    @ApiOperation("部门列表")
    @GetMapping("/list")
    public Result<List<DeptDTO>> list() {
        return Result.success(deptService.list());
    }

    @ApiOperation("保存部门")
    @PostMapping
    public Result<Void> save(@RequestBody DeptDTO dto) {
        deptService.save(dto);
        return Result.success();
    }

    @ApiOperation("更新部门")
    @PutMapping
    public Result<Void> update(@RequestBody DeptDTO dto) {
        deptService.update(dto);
        return Result.success();
    }

    @ApiOperation("删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.success();
    }
}
