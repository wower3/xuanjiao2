package com.xuanjiao.app.usage;

import com.xuanjiao.client.dto.*;

public interface UsageApplyService {
    /**
     * 申请使用素材（旧API，兼容单素材申请）
     */
    UsageApplyDTO apply(UsageApplyCmd cmd, Long userId);

    /**
     * 创建使用申请草稿（多素材支持）
     */
    UsageApplyDTO createDraft(UsageApplyCmd cmd, Long userId);

    /**
     * 更新使用申请草稿
     */
    UsageApplyDTO updateDraft(Long id, UsageApplyCmd cmd, Long userId);

    /**
     * 提交使用申请（从草稿状态提交到审批）
     */
    Long submit(Long id, Long workflowId, Long userId);

    /**
     * 删除使用申请（仅草稿状态）
     */
    void delete(Long id, Long userId);

    /**
     * 查询申请单详情
     */
    UsageApplyDTO getById(Long id);

    /**
     * 查询草稿箱
     */
    PageResult<UsageApplyDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询草稿箱（支持标题筛选）
     */
    PageResult<UsageApplyDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize, String title);

    /**
     * 查询我的申请列表（全部）
     */
    PageResult<UsageApplyDTO> queryMyApplications(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询我的申请列表（旧API，保持兼容）
     */
    PageResult<UsageApplyDTO> queryMyApplications(UsageApplyQueryCmd cmd, Long userId);

    /**
     * 检查用户是否有权限使用素材
     */
    boolean canUseAsset(Long assetId, Long userId);

    /**
     * 更新申请单状态（用于审批流程完成后调用）
     */
    void updateStatus(Long id, String status);
}
