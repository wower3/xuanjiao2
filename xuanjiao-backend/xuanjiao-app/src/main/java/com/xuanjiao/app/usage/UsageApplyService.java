package com.xuanjiao.app.usage;

import com.xuanjiao.client.dto.*;

/**
 * 素材使用申请服务接口
 * <p>提供素材使用申请的创建、修改、提交、查询等功能</p>
 * <p>支持多素材申请，一个申请可以包含多个素材</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.usage.impl.UsageApplyServiceImpl
 */
public interface UsageApplyService {

    /**
     * 申请使用素材（旧API，兼容单素材申请）
     *
     * @param cmd 申请参数
     * @param userId 申请人ID
     * @return 申请单DTO
     */
    UsageApplyDTO apply(UsageApplyCmd cmd, Long userId);

    /**
     * 创建使用申请草稿（多素材支持）
     *
     * @param cmd 申请参数
     * @param userId 创建人ID
     * @return 申请单DTO
     */
    UsageApplyDTO createDraft(UsageApplyCmd cmd, Long userId);

    /**
     * 更新使用申请草稿
     *
     * @param id 申请单ID
     * @param cmd 更新参数
     * @param userId 操作人ID
     * @return 更新后的申请单DTO
     */
    UsageApplyDTO updateDraft(Long id, UsageApplyCmd cmd, Long userId);

    /**
     * 提交使用申请（从草稿状态提交到审批）
     *
     * @param id 申请单ID
     * @param workflowId 工作流ID
     * @param userId 操作人ID
     * @return 审批实例ID
     */
    Long submit(Long id, Long workflowId, Long userId);

    /**
     * 删除使用申请（仅草稿状态）
     *
     * @param id 申请单ID
     * @param userId 操作人ID
     */
    void delete(Long id, Long userId);

    /**
     * 查询申请单详情
     *
     * @param id 申请单ID
     * @return 申请单DTO
     */
    UsageApplyDTO getById(Long id);

    /**
     * 查询草稿箱
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<UsageApplyDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询草稿箱（支持标题筛选）
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param title 标题筛选
     * @return 分页结果
     */
    PageResult<UsageApplyDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize, String title);

    /**
     * 查询我的申请列表（全部）
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<UsageApplyDTO> queryMyApplications(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询我的申请列表（旧API，保持兼容）
     *
     * @param cmd 查询参数
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<UsageApplyDTO> queryMyApplications(UsageApplyQueryCmd cmd, Long userId);

    /**
     * 检查用户是否有权限使用素材
     *
     * @param assetId 素材ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean canUseAsset(Long assetId, Long userId);

    /**
     * 更新申请单状态
     * <p>用于审批流程完成后调用</p>
     *
     * @param id 申请单ID
     * @param status 新状态
     */
    void updateStatus(Long id, String status);

    /**
     * 复制使用申请
     *
     * @param id 原申请单ID
     * @param userId 当前用户ID
     * @return 新申请单的ID
     */
    Long copyApplication(Long id, Long userId);
}
