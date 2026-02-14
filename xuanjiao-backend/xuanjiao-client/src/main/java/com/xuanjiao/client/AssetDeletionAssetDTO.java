package com.xuanjiao.client;

/**
 * 素材删除申请-素材关联数据传输对象
 *
 * <p>用于在前后端之间传输删除申请与素材的关联信息，
 * 包含素材基本信息便于前端展示。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public class AssetDeletionAssetDTO {

    /**
     * 关联记录ID
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

    /**
     * 素材名称
     */
    private String assetName;

    /**
     * 素材类型（IMAGE-图片、VIDEO-视频、DOCUMENT-文档）
     */
    private String assetType;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 缩略图路径
     */
    private String thumbnailPath;

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

    /**
     * 获取文件存储路径
     *
     * @return 文件存储路径
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * 设置文件存储路径
     *
     * @param filePath 文件存储路径
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 获取缩略图路径
     *
     * @return 缩略图路径
     */
    public String getThumbnailPath() {
        return thumbnailPath;
    }

    /**
     * 设置缩略图路径
     *
     * @param thumbnailPath 缩略图路径
     */
    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
}
