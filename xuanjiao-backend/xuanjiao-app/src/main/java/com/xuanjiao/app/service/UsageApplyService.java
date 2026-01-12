package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.*;

public interface UsageApplyService {
    /**
     * 申请使用素材
     */
    UsageApplyDTO apply(UsageApplyCmd cmd, Long userId);

    /**
     * 查询我的申请列表
     */
    PageResult<UsageApplyDTO> queryMyApplications(UsageApplyQueryCmd cmd, Long userId);

    /**
     * 检查用户是否有权限使用素材
     */
    boolean canUseAsset(Long assetId, Long userId);
}
