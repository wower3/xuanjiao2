package com.xuanjiao.adapter.web.material;

import com.xuanjiao.app.material.MaterialApplicationService;
import com.xuanjiao.client.dto.MaterialApplicationCmd;
import com.xuanjiao.client.dto.MaterialApplicationDTO;
import com.xuanjiao.client.dto.material.*;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.validation.Valid;

@Api(tags = "素材申请单管理")
@RestController
@RequestMapping("/material-application")
public class MaterialApplicationController {

    @Resource
    private MaterialApplicationService materialApplicationService;

    @ApiOperation("创建申请单（草稿）")
    @PostMapping("/create")
    public Result<MaterialApplicationDTO> create(
            @Valid @RequestBody MaterialApplicationCreateCmd cmd,
            @RequestAttribute("userId") Long userId) {
        // Convert to MaterialApplicationCmd
        MaterialApplicationCmd applicationCmd = new MaterialApplicationCmd();
        applicationCmd.setTitle(cmd.getTitle());
        applicationCmd.setMaintainerId(cmd.getMaintainerId());
        applicationCmd.setDeptId(cmd.getDeptId());
        applicationCmd.setGuaranteeDeclaration(cmd.getGuaranteeDeclaration());
        return Result.success(materialApplicationService.create(applicationCmd, userId));
    }

    @ApiOperation("更新申请单")
    @PostMapping("/update")
    public Result<MaterialApplicationDTO> update(
            @Valid @RequestBody MaterialApplicationUpdateCmd cmd,
            @RequestAttribute("userId") Long userId) {
        // Convert to MaterialApplicationCmd
        MaterialApplicationCmd applicationCmd = new MaterialApplicationCmd();
        applicationCmd.setTitle(cmd.getTitle());
        applicationCmd.setMaintainerId(cmd.getMaintainerId());
        applicationCmd.setDeptId(cmd.getDeptId());
        applicationCmd.setGuaranteeDeclaration(cmd.getGuaranteeDeclaration());
        return Result.success(materialApplicationService.update(cmd.getId(), applicationCmd, userId));
    }

    @ApiOperation("提交申请单")
    @PostMapping("/{id}/submit")
    public Result<Long> submit(
            @PathVariable Long id,
            @RequestParam Long workflowId,
            @RequestAttribute("userId") Long userId) {
        Long instanceId = materialApplicationService.submit(id, workflowId, userId);
        return Result.success(instanceId);
    }

    @ApiOperation("删除申请单")
    @PostMapping("/delete")
    public Result<Void> delete(
            @Valid @RequestBody MaterialApplicationDeleteCmd cmd,
            @RequestAttribute("userId") Long userId) {
        materialApplicationService.delete(cmd.getId(), userId);
        return Result.success();
    }

    @ApiOperation("查询申请单详情")
    @PostMapping("/getDetail")
    public Result<MaterialApplicationDTO> getDetail(@Valid @RequestBody MaterialApplicationGetDetailQry qry) {
        return Result.success(materialApplicationService.getById(qry.getId()));
    }

    @ApiOperation("查询草稿箱")
    @PostMapping("/getDrafts")
    public Result<PageResult<MaterialApplicationDTO>> queryDrafts(
            @Valid @RequestBody MaterialApplicationGetDraftsQry qry,
            @RequestAttribute("userId") Long userId) {
        return Result.success(materialApplicationService.queryDrafts(userId, qry.getPageNum(), qry.getPageSize()));
    }

    @ApiOperation("查询我的申请单")
    @PostMapping("/getMyApplications")
    public Result<PageResult<MaterialApplicationDTO>> queryMyApplications(
            @Valid @RequestBody MaterialApplicationGetMyApplicationsQry qry,
            @RequestAttribute("userId") Long userId) {
        return Result.success(materialApplicationService.queryMyApplications(userId, qry.getPageNum(), qry.getPageSize()));
    }

    @ApiOperation("复制申请单")
    @PostMapping("/{id}/copy")
    public Result<Long> copyApplication(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        Long newApplicationId = materialApplicationService.copyApplication(id, userId);
        return Result.success(newApplicationId);
    }
}
