package com.xuanjiao.adapter.web.deletion;

import com.xuanjiao.app.deletion.AssetDeletionApplicationService;
import com.xuanjiao.client.dto.AssetDeletionApplicationCmd;
import com.xuanjiao.client.dto.AssetDeletionApplicationDTO;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

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
    @PostMapping
    public Result<AssetDeletionApplicationDTO> create(
            @RequestBody AssetDeletionApplicationCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(deletionApplicationService.create(cmd, userId));
    }

    @ApiOperation("更新删除申请")
    @PutMapping("/{id}")
    public Result<AssetDeletionApplicationDTO> update(
            @PathVariable Long id,
            @RequestBody AssetDeletionApplicationCmd cmd) {
        return Result.success(deletionApplicationService.update(id, cmd));
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
    @GetMapping("/{id}")
    public Result<AssetDeletionApplicationDTO> getById(@PathVariable Long id) {
        return Result.success(deletionApplicationService.getById(id));
    }

    @ApiOperation("获取我的删除申请列表")
    @GetMapping("/my")
    public Result<PageResult<AssetDeletionApplicationDTO>> getMyApplications(
            @RequestParam(required = false, defaultValue = "") String title,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestAttribute("userId") Long userId) {
        return Result.success(deletionApplicationService.getMyApplications(title, status, pageNum, pageSize, userId));
    }

    @ApiOperation("删除草稿状态的申请")
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        deletionApplicationService.deleteById(id);
        return Result.success();
    }
}
