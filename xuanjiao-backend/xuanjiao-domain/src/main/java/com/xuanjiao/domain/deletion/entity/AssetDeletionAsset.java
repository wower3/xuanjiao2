package com.xuanjiao.domain.deletion.entity;

/**
 * 素材删除申请-素材关联实体
 * <p>删除申请与素材的多对多关系中间表</p>
 * <p>一个删除申请可以包含多个素材，一个素材可以被多个删除申请引用</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO
 */
public class AssetDeletionAsset {
    /** 关联记录唯一标识，自增主键 */
    private Long id;

    /** 关联的删除申请ID，指向AssetDeletionApplication */
    private Long deletionApplicationId;

    /** 关联的素材ID，指向Asset */
    private Long assetId;

    /** 素材名称（冗余字段，便于展示） */
    private String assetName;

    /** 素材类型（冗余字段，便于展示） */
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
