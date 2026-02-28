package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 素材删除申请-素材关联数据对象
 *
 * <p>映射数据库 asset_deletion_asset 表，用于 MyBatis 数据访问。</p>
 * <p>存储删除申请与素材的关联关系，包含素材的快照信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@TableName("asset_deletion_asset")
public class AssetDeletionAssetDO {

    /**
     * 关联ID（主键）
     */
    @TableId(type = IdType.AUTO)
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
     * 素材类型
     */
    private String assetType;

    /**
     * 素材状态（来自asset表的JOIN查询）
     */
    private String assetStatus;

    /**
     * 文件路径（来自asset表的JOIN查询）
     */
    private String filePath;

    /**
     * 缩略图路径（来自asset表的JOIN查询）
     */
    private String thumbnailPath;

    /**
     * 文件大小（来自asset表的JOIN查询）
     */
    private Long fileSize;

    /**
     * 描述（来自asset表的JOIN查询）
     */
    private String description;

    /**
     * 发布渠道（来自asset表的JOIN查询）
     */
    private String publishChannel;

    /**
     * 获取关联ID
     *
     * @return 关联ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置关联ID
     *
     * @param id 关联ID
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
     * 获取素材状态
     *
     * @return 素材状态
     */
    public String getAssetStatus() {
        return assetStatus;
    }

    /**
     * 设置素材状态
     *
     * @param assetStatus 素材状态
     */
    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }

    /**
     * 获取文件路径
     *
     * @return 文件路径
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * 设置文件路径
     *
     * @param filePath 文件路径
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

    /**
     * 获取文件大小
     *
     * @return 文件大小
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * 设置文件大小
     *
     * @param fileSize 文件大小
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 获取描述
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置描述
     *
     * @param description 描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取发布渠道
     *
     * @return 发布渠道
     */
    public String getPublishChannel() {
        return publishChannel;
    }

    /**
     * 设置发布渠道
     *
     * @param publishChannel 发布渠道
     */
    public void setPublishChannel(String publishChannel) {
        this.publishChannel = publishChannel;
    }
}
