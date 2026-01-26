package com.xuanjiao.domain.usage.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材使用申请-素材关联实体
 * 一个使用申请可以包含多个素材
 * 一个素材可以被多个使用申请使用
 */
@Data
public class UsageApplyAsset {
    private Long id;
    private Long usageApplyId;
    private Long assetId;
    private String usageDescription;         // 该素材的使用说明
    private String usagePublishChannel;      // 该素材的发布渠道
    private Integer usageIsSecondaryCreation; // 该素材是否二次创作:0-否,1-是
    private String usageAttachmentPath;      // 该素材的附件路径
    private LocalDateTime createTime;

    // 关联的素材信息（非数据库字段）
    private String assetName;
    private String assetType;
    private String assetFilePath;
    private String assetThumbnailPath;
    private String assetStatus;
}
