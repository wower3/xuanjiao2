package com.xuanjiao.infrastructure.usage;

import lombok.Data;

/**
 * UsageApplyAsset查询条件对象
 * 用于查询使用申请与素材的关联关系
 */
@Data
public class UsageApplyAssetQuery {
    /**
     * 关联ID
     */
    private Long id;

    /**
     * 使用申请ID
     */
    private Long usageApplyId;

    /**
     * 素材ID
     */
    private Long assetId;
}
