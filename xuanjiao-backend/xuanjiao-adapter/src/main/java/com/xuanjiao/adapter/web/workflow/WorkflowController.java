package com.xuanjiao.adapter.web.workflow;

import com.xuanjiao.app.workflow.WorkflowService;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.WorkflowDTO;
import com.xuanjiao.client.dto.workflow.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "流程管理")
@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    @Resource
    private WorkflowService workflowService;

    @ApiOperation("流程列表")
    @PostMapping("/getList")
    public Result<List<WorkflowDTO>> list(@Valid @RequestBody WorkflowGetListQry qry) {
        return Result.success(workflowService.list());
    }

    @ApiOperation("流程详情")
    @PostMapping("/getDetail")
    public Result<WorkflowDTO> getById(@Valid @RequestBody WorkflowGetDetailQry qry) {
        return Result.success(workflowService.getById(qry.getId()));
    }

    @ApiOperation("保存流程")
    @PostMapping("/create")
    public Result<WorkflowDTO> save(@RequestBody WorkflowDTO dto) {
        WorkflowDTO savedWorkflow = workflowService.save(dto);
        return Result.success(savedWorkflow);
    }

    @ApiOperation("更新流程")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody WorkflowUpdateCmd cmd) {
        workflowService.update(convertToDto(cmd));
        return Result.success();
    }

    @ApiOperation("更新状态")
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@Valid @RequestBody WorkflowUpdateStatusCmd cmd) {
        workflowService.updateStatus(cmd.getId(), cmd.getStatus());
        return Result.success();
    }

    @ApiOperation("删除流程")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody WorkflowDeleteCmd cmd) {
        workflowService.delete(cmd.getId());
        return Result.success();
    }

    @ApiOperation("绑定角色")
    @PostMapping("/bindRole")
    public Result<Void> bindRole(@Valid @RequestBody WorkflowBindRoleCmd cmd) {
        workflowService.bindRole(cmd.getId(), cmd.getRoleId(), cmd.getWorkflowType());
        return Result.success();
    }

    @ApiOperation("解除角色绑定")
    @PostMapping("/unbindRole")
    public Result<Void> unbindRole(@Valid @RequestBody WorkflowUnbindRoleCmd cmd) {
        workflowService.unbindRole(cmd.getId());
        return Result.success();
    }

    @ApiOperation("复制流程")
    @PostMapping("/{id}/copy")
    public Result<WorkflowDTO> copy(@PathVariable Long id) {
        return Result.success(workflowService.copy(id));
    }

    private WorkflowDTO convertToDto(WorkflowUpdateCmd cmd) {
        WorkflowDTO dto = new WorkflowDTO();
        dto.setId(cmd.getId());
        dto.setName(cmd.getName());
        dto.setDescription(cmd.getDescription());
        dto.setWorkflowType(cmd.getWorkflowType());
        dto.setStages(cmd.getStages());
        return dto;
    }
}
