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

    @ApiOperation("申请使用素材")
    @PostMapping("/apply")
    public Result<UsageApplyDTO> apply(
            @RequestBody UsageApplyCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.apply(cmd, userId));
    }

    @ApiOperation("查询我的申请列表")
    @GetMapping("/my-applications")
    public Result<PageResult<UsageApplyDTO>> queryMyApplications(
            UsageApplyQueryCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.queryMyApplications(cmd, userId));
    }

    @ApiOperation("检查是否有权限使用素材")
    @GetMapping("/can-use/{assetId}")
    public Result<Boolean> canUseAsset(
            @PathVariable Long assetId,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.canUseAsset(assetId, userId));
    }
}
