package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;

/**
 * 素材删除申请-素材关联DO（中间表，不需要逻辑删除）
 */
@TableName("asset_deletion_asset")
public class AssetDeletionAssetDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deletionApplicationId;
    private Long assetId;
    private String assetName;
    private String assetType;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeletionApplicationId() {
        return deletionApplicationId;
    }

    public void setDeletionApplicationId(Long deletionApplicationId) {
        this.deletionApplicationId = deletionApplicationId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }
}
