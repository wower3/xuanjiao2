package com.xuanjiao.infrastructure.deletion;

import lombok.Data;

/**
 * AssetDeletionAsset查询条件对象
 * 用于查询素材删除申请与素材的关联关系
 */
@Data
public class AssetDeletionAssetQuery {
    /**
     * 关联ID
     */
    private Long id;

    /**
     * 删除申请ID
     */
    private Long deletionApplicationId;

    /**
     * 素材ID
     */
    private Long assetId;
}
