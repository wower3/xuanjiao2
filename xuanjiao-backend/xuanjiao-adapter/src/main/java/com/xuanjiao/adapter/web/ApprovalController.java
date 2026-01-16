package com.xuanjiao.adapter.web;

import com.xuanjiao.app.service.ApprovalService;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.Map;

@Api(tags = "审批管理")
@RestController
@RequestMapping("/approval")
public class ApprovalController {

    @Resource
    private ApprovalService approvalService;

    @ApiOperation("待我审批")
    @GetMapping("/tasks")
    public Result<PageResult<Map<String, Object>>> getMyTasks(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(approvalService.getMyTasks(userId, pageNum, pageSize));
    }

    @ApiOperation("我发起的")
    @GetMapping("/applied")
    public Result<PageResult<Map<String, Object>>> getMyApplied(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false, defaultValue = "false") boolean forAllUsers,
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String roleType,
            @RequestParam(required = false) String status) {
        return Result.success(approvalService.getMyApplied(userId, pageNum, pageSize,
                businessType, forAllUsers, applicantId, deptId, roleType, status));
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

    @ApiOperation("获取审批任务详情")
    @GetMapping("/tasks/{id}/detail")
    public Result<Map<String, Object>> getTaskDetail(@PathVariable Long id) {
        return Result.success(approvalService.getTaskDetail(id));
    }

    @ApiOperation("获取审批实例详情")
    @GetMapping("/instances/{id}/detail")
    public Result<Map<String, Object>> getInstanceDetail(@PathVariable Long id) {
        return Result.success(approvalService.getInstanceDetail(id));
    }
}
