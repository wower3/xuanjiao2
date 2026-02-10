package com.xuanjiao.domain.deletion.entity;

/**
 * 素材删除申请-素材关联实体
 *
 * <p>删除申请与素材的多对多关系中间表。</p>
 * <p>一个删除申请可以包含多个素材，一个素材可以被多个删除申请引用。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public class AssetDeletionAsset {

    /**
     * 关联记录唯一标识
     *
     * <p>自增主键。</p>
     */
    private Long id;

    /**
     * 关联的删除申请ID
     *
     * <p>指向AssetDeletionApplication。</p>
     */
    private Long deletionApplicationId;

    /**
     * 关联的素材ID
     *
     * <p>指向Asset。</p>
     */
    private Long assetId;

    /**
     * 素材名称
     *
     * <p>冗余字段，便于展示。</p>
     */
    private String assetName;

    /**
     * 素材类型
     *
     * <p>冗余字段，便于展示。</p>
     */
    private String assetType;

    /**
     * 获取关联记录ID
     *
     * @return 关联记录ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置关联记录ID
     *
     * @param id 关联记录ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取删除申请ID
     *
     * @return 删除申请ID
     */
    public Long getDeletionApplicationId() {
        return deletionApplicationId;
    }

    /**
     * 设置删除申请ID
     *
     * @param deletionApplicationId 删除申请ID
     */
    public void setDeletionApplicationId(Long deletionApplicationId) {
        this.deletionApplicationId = deletionApplicationId;
    }

    /**
     * 获取素材ID
     *
     * @return 素材ID
     */
    public Long getAssetId() {
        return assetId;
    }

    /**
     * 设置素材ID
     *
     * @param assetId 素材ID
     */
    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    /**
     * 获取素材名称
     *
     * @return 素材名称
     */
    public String getAssetName() {
        return assetName;
    }

    /**
     * 设置素材名称
     *
     * @param assetName 素材名称
     */
    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    /**
     * 获取素材类型
     *
     * @return 素材类型
     */
    public String getAssetType() {
        return assetType;
    }

    /**
     * 设置素材类型
     *
     * @param assetType 素材类型
     */
    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }
}
