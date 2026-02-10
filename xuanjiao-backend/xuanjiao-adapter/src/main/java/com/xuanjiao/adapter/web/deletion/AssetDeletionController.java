package com.xuanjiao.adapter.web.deletion;

import com.xuanjiao.app.deletion.AssetDeletionApplicationService;
import com.xuanjiao.client.dto.AssetDeletionApplicationCmd;
import com.xuanjiao.client.dto.AssetDeletionApplicationDTO;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.deletion.DeletionDeleteCmd;
import com.xuanjiao.client.dto.deletion.DeletionGetDetailQry;
import com.xuanjiao.client.dto.deletion.DeletionGetMyApplicationsQry;
import com.xuanjiao.client.dto.deletion.DeletionUpdateCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
 * 素材删除申请控制器
 *
 * <p>提供素材删除申请的创建、修改、提交审批等功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>创建申请单：创建新的素材删除申请</li>
 *   <li>更新申请单：修改申请单信息（仅草稿状态可修改）</li>
 *   <li>提交审批：将申请单提交审批流程</li>
 *   <li>查询详情：查询单个申请单的详细信息</li>
 *   <li>查询列表：分页查询申请单列表</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "素材删除申请")
@RestController
@RequestMapping("/deletion")
public class AssetDeletionController {

    /**
     * 素材删除申请服务
     *
     * <p>处理素材删除申请的业务逻辑。</p>
     */
    @Resource
    private AssetDeletionApplicationService deletionApplicationService;

    /**
     * 创建删除申请
     *
     * <p>创建新的素材删除申请，需要提供申请标题、要删除的素材ID列表、
     * 删除原因等信息。申请创建后为草稿状态。</p>
     *
     * @param cmd 创建命令，包含申请信息
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 创建后的申请单信息
     */
    @ApiOperation("创建删除申请")
    @PostMapping("/create")
    public Result<AssetDeletionApplicationDTO> create(
            @Valid @RequestBody AssetDeletionApplicationCmd cmd,
            @RequestAttribute("userId") Long userId) {
        return Result.success(deletionApplicationService.create(cmd, userId));
    }

    /**
     * 更新删除申请
     *
     * <p>修改指定素材删除申请的信息，仅草稿状态可以修改。
     * 可修改内容包括申请标题、删除原因、附件、素材列表等。</p>
     *
     * @param cmd 更新命令，包含申请单ID和要更新的字段
     * @return 更新后的申请单信息
     */
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

    /**
     * 提交审批
     *
     * <p>将草稿状态的素材删除申请提交到指定的审批流程。
     * 提交后申请单状态变为待审批，开始审批流程。</p>
     *
     * @param id 申请单ID
     * @param workflowId 审批流程ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 审批实例ID
     */
    @ApiOperation("提交审批")
    @PostMapping("/{id}/submit")
    public Result<Long> submitApproval(
            @PathVariable Long id,
            @RequestParam Long workflowId,
            @RequestAttribute("userId") Long userId) {
        Long instanceId = deletionApplicationService.submitApproval(id, workflowId, userId);
        return Result.success(instanceId);
    }

    /**
     * 获取删除申请详情
     *
     * <p>根据申请单ID查询详细信息，包括申请单基本信息、要删除的素材列表、
     * 删除原因、审批状态等。</p>
     *
     * @param qry 查询条件，包含申请单ID
     * @return 申请单详情信息
     */
    @ApiOperation("获取删除申请详情")
    @PostMapping("/getDetail")
    public Result<AssetDeletionApplicationDTO> getDetail(@Valid @RequestBody DeletionGetDetailQry qry) {
        return Result.success(deletionApplicationService.getById(qry.getId()));
    }

    /**
     * 获取我的删除申请列表
     *
     * <p>分页查询当前用户发起的所有素材删除申请，支持按标题和状态筛选。</p>
     *
     * @param qry 查询条件，包含分页参数、标题和状态筛选条件
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 分页的申请单列表
     */
    @ApiOperation("获取我的删除申请列表")
    @PostMapping("/getMyApplications")
    public Result<PageResult<AssetDeletionApplicationDTO>> getMyApplications(
            @Valid @RequestBody DeletionGetMyApplicationsQry qry,
            @RequestAttribute("userId") Long userId) {
        return Result.success(deletionApplicationService.getMyApplications(qry.getTitle(), qry.getStatus(), qry.getPageNum(), qry.getPageSize(), userId));
    }

    /**
     * 删除草稿状态的申请
     *
     * <p>删除指定的素材删除申请，仅草稿状态的申请可以删除。
     * 已提交审批的申请不能删除。</p>
     *
     * @param cmd 删除命令，包含要删除的申请单ID
     * @return 操作结果
     */
    @ApiOperation("删除草稿状态的申请")
    @PostMapping("/delete")
    public Result<Void> deleteById(@Valid @RequestBody DeletionDeleteCmd cmd) {
        deletionApplicationService.deleteById(cmd.getId());
        return Result.success();
    }

    /**
     * 复制删除申请
     *
     * <p>复制指定申请单创建一个新的草稿申请单。
     * 仅已驳回状态的申请单可以复制。</p>
     *
     * @param id 要复制的申请单ID
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 新创建的申请单ID
     */
    @ApiOperation("复制删除申请")
    @PostMapping("/{id}/copy")
    public Result<Long> copyApplication(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        Long newApplicationId = deletionApplicationService.copyApplication(id, userId);
        return Result.success(newApplicationId);
    }
}
