package com.xuanjiao.adapter.web.approval;

import com.xuanjiao.app.approval.ApprovalService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.approval.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Api(tags = "审批管理")
@RestController
@RequestMapping("/approval")
public class ApprovalController {

    @Resource
    private ApprovalService approvalService;

    @Resource
    private WorkflowEngineService workflowEngineService;

    @ApiOperation("待我审批")
    @PostMapping("/getMyTasks")
    public Result<PageResult<Map<String, Object>>> getMyTasks(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ApprovalGetMyTasksQry qry) {
        return Result.success(approvalService.getMyTasks(userId, qry.getPageNum(), qry.getPageSize()));
    }

    @ApiOperation("我发起的")
    @PostMapping("/getMyApplied")
    public Result<PageResult<Map<String, Object>>> getMyApplied(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ApprovalGetMyAppliedQry qry) {
        return Result.success(approvalService.getMyApplied(userId, qry.getPageNum(), qry.getPageSize(),
                qry.getBusinessType(), qry.getForAllUsers(), qry.getApplicantId(),
                qry.getDeptId(), qry.getRoleType(), qry.getStatus()));
    }

    @ApiOperation("审批")
    @PostMapping("/tasks/{id}/approve")
    public Result<Void> approve(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestParam String comment,
            @RequestParam boolean passed) {
        approvalService.approve(id, userId, comment, passed);
        return Result.success();
    }

    @ApiOperation("退回上一级")
    @PostMapping("/tasks/{id}/return")
    public Result<Void> returnTask(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String comment) {
        approvalService.returnTask(id, userId, comment);
        return Result.success();
    }

    @ApiOperation("获取审批任务详情")
    @PostMapping("/getTaskDetail")
    public Result<Map<String, Object>> getTaskDetail(@Valid @RequestBody ApprovalGetTaskDetailQry qry) {
        return Result.success(approvalService.getTaskDetail(qry.getId()));
    }

    @ApiOperation("获取审批实例详情")
    @PostMapping("/getInstanceDetail")
    public Result<Map<String, Object>> getInstanceDetail(@Valid @RequestBody ApprovalGetInstanceDetailQry qry) {
        return Result.success(approvalService.getInstanceDetail(qry.getId()));
    }

    @ApiOperation("追回工单（发起人追回正在审批的工单）")
    @PostMapping("/instances/{id}/withdraw")
    public Result<Void> withdrawInstance(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String comment) {
        workflowEngineService.withdrawInstance(id, userId, comment);
        return Result.success();
    }

    @ApiOperation("重新发起子流程")
    @PostMapping("/tasks/{id}/restart-sub-workflow")
    public Result<Void> restartSubWorkflow(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId,
            @RequestBody List<Long> approverIds) {
        workflowEngineService.restartSubWorkflow(id, userId, approverIds);
        return Result.success();
    }
}
