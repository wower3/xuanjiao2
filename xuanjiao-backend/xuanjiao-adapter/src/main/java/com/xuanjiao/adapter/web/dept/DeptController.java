package com.xuanjiao.adapter.web.dept;

import com.xuanjiao.app.dept.DeptService;
import com.xuanjiao.client.dto.DeptDTO;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.dept.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "部门管理")
@RestController
@RequestMapping("/dept")
public class DeptController {

    @Resource
    private DeptService deptService;

    @ApiOperation("部门列表")
    @PostMapping("/getList")
    public Result<List<DeptDTO>> list(@Valid @RequestBody DeptGetListQry qry) {
        return Result.success(deptService.list());
    }

    @ApiOperation("部门树")
    @PostMapping("/getTree")
    public Result<List<DeptDTO>> tree(@Valid @RequestBody DeptGetTreeQry qry) {
        return Result.success(deptService.getTree());
    }

    @ApiOperation("获取部门详情")
    @PostMapping("/getDetail")
    public Result<DeptDTO> getById(@Valid @RequestBody DeptGetDetailQry qry) {
        return Result.success(deptService.getById(qry.getId()));
    }

    @ApiOperation("保存部门")
    @PostMapping("/create")
    public Result<Void> save(@RequestBody DeptDTO dto) {
        deptService.save(dto);
        return Result.success();
    }

    @ApiOperation("更新部门")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody DeptUpdateCmd cmd) {
        deptService.update(convertToDto(cmd));
        return Result.success();
    }

    @ApiOperation("删除部门")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody DeptDeleteCmd cmd) {
        deptService.delete(cmd.getId());
        return Result.success();
    }

    @ApiOperation("生成部门编号")
    @GetMapping("/generate-code")
    public Result<String> generateCode() {
        return Result.success(deptService.generateCode());
    }

    private DeptDTO convertToDto(DeptUpdateCmd cmd) {
        DeptDTO dto = new DeptDTO();
        dto.setId(cmd.getId());
        dto.setName(cmd.getName());
        dto.setCode(cmd.getCode());
        dto.setParentId(cmd.getParentId());
        dto.setSort(cmd.getSort());
        return dto;
    }
}
