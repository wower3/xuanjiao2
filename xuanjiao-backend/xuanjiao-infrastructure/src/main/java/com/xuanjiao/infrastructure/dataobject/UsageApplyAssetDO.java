package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材使用申请-素材关联DO
 */
@Data
@TableName("usage_apply_asset")
public class UsageApplyAssetDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long usageApplyId;
    private Long assetId;
    private String usageDescription;
    private String usagePublishChannel;
    private Integer usageIsSecondaryCreation;
    private String usageAttachmentPath;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 非数据库字段，用于关联查询
    @TableField(exist = false)
    private String assetName;
    @TableField(exist = false)
    private String assetType;
    @TableField(exist = false)
    private String assetFilePath;
    @TableField(exist = false)
    private String assetThumbnailPath;
    @TableField(exist = false)
    private String assetStatus;
}
