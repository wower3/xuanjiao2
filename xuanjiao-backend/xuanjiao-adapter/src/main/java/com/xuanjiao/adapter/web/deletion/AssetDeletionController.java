package com.xuanjiao.adapter.web.deletion;

import com.xuanjiao.app.deletion.AssetDeletionApplicationService;
import com.xuanjiao.client.dto.AssetDeletionApplicationCmd;
import com.xuanjiao.client.dto.AssetDeletionApplicationDTO;
import com.xuanjiao.client.dto.deletion.*;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 素材删除申请Controller
 */
@Api(tags = "素材删除申请")
@RestController
@RequestMapping("/deletion")
public class AssetDeletionController {

    @Resource
    private AssetDeletionApplicationService deletionApplicationService;

    @ApiOperation("创建删除申请")
    @PostMapping("/create")
    public Result<AssetDeletionApplicationDTO> create(
            @Valid @RequestBody AssetDeletionApplicationCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(deletionApplicationService.create(cmd, userId));
    }

    @ApiOperation("更新删除申请")
    @PostMapping("/update")
    public Result<AssetDeletionApplicationDTO> update(
            @Valid @RequestBody DeletionUpdateCmd cmd) {
        AssetDeletionApplicationCmd applicationCmd = new AssetDeletionApplicationCmd();
        applicationCmd.setTitle(cmd.getTitle());
        applicationCmd.setWorkflowId(cmd.getWorkflowId());
        applicationCmd.setDeleteReason(cmd.getDeleteReason());
        applicationCmd.setAttachmentPath(cmd.getAttachmentPath());
        applicationCmd.setAssetIds(cmd.getAssetIds());
        return Result.success(deletionApplicationService.update(cmd.getId(), applicationCmd));
    }

    @ApiOperation("提交审批")
    @PostMapping("/{id}/submit")
    public Result<Long> submitApproval(
            @PathVariable Long id,
            @RequestParam Long workflowId,
            @RequestAttribute("userId") Long userId) {
        Long instanceId = deletionApplicationService.submitApproval(id, workflowId, userId);
        return Result.success(instanceId);
    }

    @ApiOperation("获取删除申请详情")
    @PostMapping("/getDetail")
    public Result<AssetDeletionApplicationDTO> getDetail(@Valid @RequestBody DeletionGetDetailQry qry) {
        return Result.success(deletionApplicationService.getById(qry.getId()));
    }

    @ApiOperation("获取我的删除申请列表")
    @PostMapping("/getMyApplications")
    public Result<PageResult<AssetDeletionApplicationDTO>> getMyApplications(
            @Valid @RequestBody DeletionGetMyApplicationsQry qry,
            @RequestAttribute("userId") Long userId) {
        return Result.success(deletionApplicationService.getMyApplications(qry.getTitle(), qry.getStatus(), qry.getPageNum(), qry.getPageSize(), userId));
    }

    @ApiOperation("删除草稿状态的申请")
    @PostMapping("/delete")
    public Result<Void> deleteById(@Valid @RequestBody DeletionDeleteCmd cmd) {
        deletionApplicationService.deleteById(cmd.getId());
        return Result.success();
    }

    @ApiOperation("复制删除申请")
    @PostMapping("/{id}/copy")
    public Result<Long> copyApplication(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        Long newApplicationId = deletionApplicationService.copyApplication(id, userId);
        return Result.success(newApplicationId);
    }
}
