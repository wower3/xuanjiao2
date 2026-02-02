package com.xuanjiao.adapter.web.usage;

import com.xuanjiao.app.usage.UsageApplyService;
import com.xuanjiao.client.dto.*;
import com.xuanjiao.client.dto.usage.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

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
            @Valid @RequestBody UsageApplyCreateDraftCmd cmd,
            @RequestAttribute("userId") Long userId) {
        // Convert to UsageApplyCmd
        UsageApplyCmd applyCmd = new UsageApplyCmd();
        applyCmd.setTitle(cmd.getTitle());
        applyCmd.setAssetConfigs(cmd.getAssetConfigs());
        return Result.success(usageApplyService.createDraft(applyCmd, userId));
    }

    @ApiOperation("更新使用申请草稿")
    @PostMapping("/update")
    public Result<UsageApplyDTO> updateDraft(
            @Valid @RequestBody UsageApplyUpdateCmd cmd,
            @RequestAttribute("userId") Long userId) {
        // Convert to UsageApplyCmd
        UsageApplyCmd applyCmd = new UsageApplyCmd();
        applyCmd.setTitle(cmd.getTitle());
        applyCmd.setAssetConfigs(cmd.getAssetConfigs());
        return Result.success(usageApplyService.updateDraft(cmd.getId(), applyCmd, userId));
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
    @PostMapping("/delete")
    public Result<Void> delete(
            @Valid @RequestBody UsageApplyDeleteCmd cmd,
            @RequestAttribute("userId") Long userId) {
        usageApplyService.delete(cmd.getId(), userId);
        return Result.success();
    }

    @ApiOperation("查询申请单详情")
    @PostMapping("/getDetail")
    public Result<UsageApplyDTO> getDetail(@Valid @RequestBody UsageApplyGetDetailQry qry) {
        return Result.success(usageApplyService.getById(qry.getId()));
    }

    @ApiOperation("查询草稿箱")
    @PostMapping("/getDrafts")
    public Result<PageResult<UsageApplyDTO>> queryDrafts(
            @Valid @RequestBody UsageApplyGetDraftsQry qry,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.queryDrafts(userId, qry.getPageNum(), qry.getPageSize()));
    }

    @ApiOperation("查询我的所有申请")
    @PostMapping("/getMyApplications")
    public Result<PageResult<UsageApplyDTO>> queryMyApplications(
            @Valid @RequestBody UsageApplyGetMyApplicationsQry qry,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.queryMyApplications(userId, qry.getPageNum(), qry.getPageSize()));
    }

    // ========== 通用API ==========

    @ApiOperation("检查是否有权限使用素材")
    @PostMapping("/canUseAsset")
    public Result<Boolean> canUseAsset(
            @Valid @RequestBody UsageApplyCanUseAssetQry qry,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.canUseAsset(qry.getAssetId(), userId));
    }

    @ApiOperation("复制使用申请")
    @PostMapping("/{id}/copy")
    public Result<Long> copyApplication(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        Long newApplicationId = usageApplyService.copyApplication(id, userId);
        return Result.success(newApplicationId);
    }
}
