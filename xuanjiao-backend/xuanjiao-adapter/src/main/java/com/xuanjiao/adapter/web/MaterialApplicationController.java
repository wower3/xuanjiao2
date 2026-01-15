package com.xuanjiao.adapter.web;

import com.xuanjiao.app.service.MaterialApplicationService;
import com.xuanjiao.client.dto.MaterialApplicationCmd;
import com.xuanjiao.client.dto.MaterialApplicationDTO;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@Api(tags = "素材申请单管理")
@RestController
@RequestMapping("/material-application")
public class MaterialApplicationController {

    @Resource
    private MaterialApplicationService materialApplicationService;

    @ApiOperation("创建申请单（草稿）")
    @PostMapping
    public Result<MaterialApplicationDTO> create(
            @RequestBody MaterialApplicationCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(materialApplicationService.create(cmd, userId));
    }

    @ApiOperation("更新申请单")
    @PutMapping("/{id}")
    public Result<MaterialApplicationDTO> update(
            @PathVariable Long id,
            @RequestBody MaterialApplicationCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(materialApplicationService.update(id, cmd, userId));
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
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        materialApplicationService.delete(id, userId);
        return Result.success();
    }

    @ApiOperation("查询申请单详情")
    @GetMapping("/{id}")
    public Result<MaterialApplicationDTO> getById(@PathVariable Long id) {
        return Result.success(materialApplicationService.getById(id));
    }

    @ApiOperation("查询草稿箱")
    @GetMapping("/drafts")
    public Result<PageResult<MaterialApplicationDTO>> queryDrafts(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(materialApplicationService.queryDrafts(userId, pageNum, pageSize));
    }

    @ApiOperation("查询我的申请单")
    @GetMapping("/my")
    public Result<PageResult<MaterialApplicationDTO>> queryMyApplications(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(materialApplicationService.queryMyApplications(userId, pageNum, pageSize));
    }
}
