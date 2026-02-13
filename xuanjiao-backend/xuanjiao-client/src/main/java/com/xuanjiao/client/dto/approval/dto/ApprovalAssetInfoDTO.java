package com.xuanjiao.client.dto.approval.dto;

import lombok.Data;

/**
 * 审批素材信息数据传输对象
 *
 * <p>用于在审批详情中展示关联素材的基本信息，
 * 包括素材ID、名称、类型等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalAssetInfoDTO {

    /**
     * 素材ID
     */
    private Long id;

    /**
     * 素材名称
     */
    private String name;

    /**
     * 素材类型（IMAGE-图片、VIDEO-视频、DOCUMENT-文档）
     */
    private String type;

    /**
     * 素材状态
     */
    private String status;

    /**
     * 文件路径（用于预览和下载）
     */
    private String filePath;

    /**
     * 缩略图路径
     */
    private String thumbnailPath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 素材描述
     */
    private String description;

    /**
     * 发布渠道
     */
    private String publishChannel;

    /**
     * 版权文件路径
     */
    private String copyrightFilePath;

    /**
     * 使用说明（ASSET_USAGE 类型）
     */
    private String usageDescription;

    /**
     * 使用发布渠道（ASSET_USAGE 类型）
     */
    private String usagePublishChannel;
}
