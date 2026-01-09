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
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(approvalService.getMyApplied(userId, pageNum, pageSize));
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
}
