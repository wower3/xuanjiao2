package com.xuanjiao.adapter.web;

import com.xuanjiao.app.service.WorkflowService;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.WorkflowDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@Api(tags = "流程管理")
@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    @Resource
    private WorkflowService workflowService;

    @ApiOperation("流程列表")
    @GetMapping("/list")
    public Result<List<WorkflowDTO>> list() {
        return Result.success(workflowService.list());
    }

    @ApiOperation("流程详情")
    @GetMapping("/{id}")
    public Result<WorkflowDTO> getById(@PathVariable Long id) {
        return Result.success(workflowService.getById(id));
    }

    @ApiOperation("保存流程")
    @PostMapping
    public Result<Void> save(@RequestBody WorkflowDTO dto) {
        workflowService.save(dto);
        return Result.success();
    }

    @ApiOperation("更新流程")
    @PutMapping
    public Result<Void> update(@RequestBody WorkflowDTO dto) {
        workflowService.update(dto);
        return Result.success();
    }

    @ApiOperation("更新状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        workflowService.updateStatus(id, status);
        return Result.success();
    }

    @ApiOperation("删除流程")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        workflowService.delete(id);
        return Result.success();
    }

    @ApiOperation("绑定角色")
    @PutMapping("/{id}/bind-role")
    public Result<Void> bindRole(@PathVariable Long id, @RequestParam Long roleId, @RequestParam String workflowType) {
        workflowService.bindRole(id, roleId, workflowType);
        return Result.success();
    }

    @ApiOperation("解除角色绑定")
    @PutMapping("/{id}/unbind-role")
    public Result<Void> unbindRole(@PathVariable Long id) {
        workflowService.unbindRole(id);
        return Result.success();
    }

    @ApiOperation("复制流程")
    @PostMapping("/{id}/copy")
    public Result<WorkflowDTO> copy(@PathVariable Long id) {
        return Result.success(workflowService.copy(id));
    }
}
