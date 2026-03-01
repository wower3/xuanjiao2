package com.xuanjiao.adapter.web.usage;

import com.xuanjiao.app.usage.UsageApplyService;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.Result;
import com.xuanjiao.client.usage.UsageApplyCmd;
import com.xuanjiao.client.usage.UsageApplyDTO;
import com.xuanjiao.client.usage.UsageApplyQueryCmd;
import com.xuanjiao.client.usage.UsageApplyCanUseAssetQry;
import com.xuanjiao.client.usage.UsageApplyCreateDraftCmd;
import com.xuanjiao.client.usage.UsageApplyDeleteCmd;
import com.xuanjiao.client.usage.UsageApplyGetDetailQry;
import com.xuanjiao.client.usage.UsageApplyGetDraftsQry;
import com.xuanjiao.client.usage.UsageApplyGetMyApplicationsQry;
import com.xuanjiao.client.usage.UsageApplyUpdateCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 素材使用申请控制器
 *
 * <p>提供素材使用申请的创建、修改、提交审批等功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>创建申请单：创建新的素材使用申请（支持多素材）</li>
 *   <li>更新申请单：修改申请单信息（仅草稿状态可修改）</li>
 *   <li>提交审批：将申请单提交审批流程</li>
 *   <li>查询详情：查询单个申请单的详细信息</li>
 *   <li>查询列表：分页查询申请单列表</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "素材使用申请")
@RestController
@RequestMapping("/usage-apply")
public class UsageApplyController {

    /**
     * 素材使用申请服务
     *
     * <p>处理素材使用申请的业务逻辑。</p>
     */
    @Resource
    private UsageApplyService usageApplyService;

    // ========== 旧API（兼容单素材申请） ==========

    /**
     * 申请使用素材（旧API，单素材）
     *
     * <p>创建素材使用申请的旧版API，仅支持单个素材申请。
     * 建议使用新API（draft）支持多素材申请。</p>
     *
     * @param cmd 申请命令，包含素材ID和使用信息
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 创建后的申请单信息
     * @deprecated 建议使用 {@link #createDraft} 支持多素材申请
     */
    @Deprecated
    @ApiOperation("申请使用素材（旧API，单素材）")
    @PostMapping("/apply")
    public Result<UsageApplyDTO> apply(
            @RequestBody UsageApplyCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.apply(cmd, userId));
    }

    /**
     * 查询我的申请列表（旧API，按条件查询）
     *
     * <p>分页查询当前用户的素材使用申请列表的旧版API。</p>
     *
     * @param cmd 查询命令，包含分页参数和筛选条件
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 分页的申请单列表
     * @deprecated 建议使用 {@link #queryMyApplications}
     */
    @Deprecated
    @ApiOperation("查询我的申请列表（旧API，按条件查询）")
    @GetMapping("/my-applications")
    public Result<PageResult<UsageApplyDTO>> queryMyApplications(
            UsageApplyQueryCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.queryMyApplications(cmd, userId));
    }

    // ========== 新API（多素材支持） ==========

    /**
     * 创建使用申请草稿
     *
     * <p>创建新的素材使用申请草稿，支持同时申请多个素材。
     * 每个素材可以单独配置使用描述、发布渠道、是否二次创作等信息。</p>
     *
     * @param cmd 创建命令，包含申请标题和素材配置列表
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 创建后的申请单信息
     */
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

    /**
     * 更新使用申请草稿
     *
     * <p>修改指定素材使用申请草稿的信息，仅草稿状态可以修改。
     * 可修改内容包括申请标题和各素材的配置信息。</p>
     *
     * @param cmd 更新命令，包含申请单ID和要更新的字段
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 更新后的申请单信息
     */
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

    /**
     * 提交使用申请
     *
     * <p>将草稿状态的素材使用申请提交到指定的审批流程。
     * 提交后申请单状态变为待审批，开始审批流程。</p>
     *
     * @param id 申请单ID
     * @param workflowId 审批流程ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 审批实例ID
     */
    @ApiOperation("提交使用申请")
    @PostMapping("/{id}/submit")
    public Result<Long> submit(
            @PathVariable Long id,
            @RequestParam Long workflowId,
            @RequestAttribute("userId") Long userId) {
        Long instanceId = usageApplyService.submit(id, workflowId, userId);
        return Result.success(instanceId);
    }

    /**
     * 删除使用申请（仅草稿）
     *
     * <p>删除指定的素材使用申请，仅草稿状态的申请可以删除。
     * 已提交审批的申请不能删除。</p>
     *
     * @param cmd 删除命令，包含要删除的申请单ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 操作结果
     */
    @ApiOperation("删除使用申请（仅草稿）")
    @PostMapping("/delete")
    public Result<Void> delete(
            @Valid @RequestBody UsageApplyDeleteCmd cmd,
            @RequestAttribute("userId") Long userId) {
        usageApplyService.delete(cmd.getId(), userId);
        return Result.success();
    }

    /**
     * 查询申请单详情
     *
     * <p>根据申请单ID查询详细信息，包括申请单基本信息、关联的素材列表、
     * 各素材的使用配置、审批状态等。</p>
     *
     * @param qry 查询条件，包含申请单ID
     * @return 申请单详情信息
     */
    @ApiOperation("查询申请单详情")
    @PostMapping("/getDetail")
    public Result<UsageApplyDTO> getDetail(@Valid @RequestBody UsageApplyGetDetailQry qry) {
        return Result.success(usageApplyService.getById(qry.getId()));
    }

    /**
     * 查询草稿箱
     *
     * <p>分页查询当前用户保存的草稿申请单。</p>
     *
     * @param qry 查询条件，包含分页参数
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 分页的草稿申请单列表
     */
    @ApiOperation("查询草稿箱")
    @PostMapping("/getDrafts")
    public Result<PageResult<UsageApplyDTO>> queryDrafts(
            @Valid @RequestBody UsageApplyGetDraftsQry qry,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.queryDrafts(userId, qry.getPageNum(), qry.getPageSize()));
    }

    /**
     * 查询我的所有申请
     *
     * <p>分页查询当前用户发起的所有素材使用申请，包括草稿、待审批、
     * 已通过、已驳回等状态的申请单。</p>
     *
     * @param qry 查询条件，包含分页参数
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 分页的申请单列表
     */
    @ApiOperation("查询我的所有申请")
    @PostMapping("/getMyApplications")
    public Result<PageResult<UsageApplyDTO>> queryMyApplications(
            @Valid @RequestBody UsageApplyGetMyApplicationsQry qry,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.queryMyApplications(userId, qry.getPageNum(), qry.getPageSize()));
    }

    // ========== 通用API ==========

    /**
     * 检查是否有权限使用素材
     *
     * <p>检查当前用户是否有权限使用指定素材。
     * 用户需要通过素材使用审批后才能下载和使用素材。</p>
     *
     * @param qry 查询条件，包含素材ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @return true 表示有权限使用，false 表示无权限
     */
    @ApiOperation("检查是否有权限使用素材")
    @PostMapping("/canUseAsset")
    public Result<Boolean> canUseAsset(
            @Valid @RequestBody UsageApplyCanUseAssetQry qry,
            @RequestAttribute("userId") Long userId) {
        return Result.success(usageApplyService.canUseAsset(qry.getAssetId(), userId));
    }

    /**
     * 复制使用申请
     *
     * <p>复制指定申请单创建一个新的草稿申请单。
     * 仅已驳回状态的申请单可以复制。</p>
     *
     * @param id 要复制的申请单ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 新创建的申请单ID
     */
    @ApiOperation("复制使用申请")
    @PostMapping("/{id}/copy")
    public Result<Long> copyApplication(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        Long newApplicationId = usageApplyService.copyApplication(id, userId);
        return Result.success(newApplicationId);
    }
}
