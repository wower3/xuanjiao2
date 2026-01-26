package com.xuanjiao.adapter.web.usage;

import com.xuanjiao.app.usage.UsageApplyService;
import com.xuanjiao.client.dto.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@Api(tags = "素材使用申请")
@RestController
@RequestMapping("/usage-apply")
public class UsageApplyController {

    @Resource
    private UsageApplyService usageApplyService;

    // ========== 旧API（兼容单素材申请） ==========

    @ApiOperation("申请使用素材（旧API，单素材）")
    @PostMapping("/apply")
    public Result<UsageApplyDTO> apply(
            @RequestBody UsageApplyCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.apply(cmd, userId));
    }

    @ApiOperation("查询我的申请列表（旧API，按条件查询）")
    @GetMapping("/my-applications")
    public Result<PageResult<UsageApplyDTO>> queryMyApplications(
            UsageApplyQueryCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.queryMyApplications(cmd, userId));
    }

    // ========== 新API（多素材支持） ==========

    @ApiOperation("创建使用申请草稿")
    @PostMapping("/draft")
    public Result<UsageApplyDTO> createDraft(
            @RequestBody UsageApplyCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.createDraft(cmd, userId));
    }

    @ApiOperation("更新使用申请草稿")
    @PutMapping("/{id}")
    public Result<UsageApplyDTO> updateDraft(
            @PathVariable Long id,
            @RequestBody UsageApplyCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.updateDraft(id, cmd, userId));
    }

    @ApiOperation("提交使用申请")
    @PostMapping("/{id}/submit")
    public Result<Long> submit(
            @PathVariable Long id,
            @RequestParam Long workflowId,
            @RequestAttribute("userId") Long userId) {
        Long instanceId = usageApplyService.submit(id, workflowId, userId);
        return Result.success(instanceId);
    }

    @ApiOperation("删除使用申请（仅草稿）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        usageApplyService.delete(id, userId);
        return Result.success();
    }

    @ApiOperation("查询申请单详情")
    @GetMapping("/{id}")
    public Result<UsageApplyDTO> getById(@PathVariable Long id) {
        return Result.success(usageApplyService.getById(id));
    }

    @ApiOperation("查询草稿箱")
    @GetMapping("/drafts")
    public Result<PageResult<UsageApplyDTO>> queryDrafts(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(usageApplyService.queryDrafts(userId, pageNum, pageSize));
    }

    @ApiOperation("查询我的所有申请")
    @GetMapping("/my")
    public Result<PageResult<UsageApplyDTO>> queryMyApplications(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(usageApplyService.queryMyApplications(userId, pageNum, pageSize));
    }

    // ========== 通用API ==========

    @ApiOperation("检查是否有权限使用素材")
    @GetMapping("/can-use/{assetId}")
    public Result<Boolean> canUseAsset(
            @PathVariable Long assetId,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.canUseAsset(assetId, userId));
    }
}
