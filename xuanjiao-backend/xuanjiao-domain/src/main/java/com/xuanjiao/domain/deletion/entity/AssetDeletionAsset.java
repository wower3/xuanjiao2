package com.xuanjiao.domain.deletion.entity;

/**
 * 素材删除申请-素材关联实体
 */
public class AssetDeletionAsset {
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
