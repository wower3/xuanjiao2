package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材使用申请-素材关联数据对象
 * <p>对应数据库表 usage_apply_asset，存储使用申请与素材的关联关系</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.usage.entity.UsageApplyAsset
 */
@Data
@TableName("usage_apply_asset")
public class UsageApplyAssetDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的使用申请ID */
    private Long usageApplyId;

    /** 关联的素材ID */
    private Long assetId;

    /** 该素材的使用说明 */
    private String usageDescription;

    /** 该素材的发布渠道 */
    private String usagePublishChannel;

    /** 该素材是否二次创作：0-否、1-是 */
    private Integer usageIsSecondaryCreation;

    /** 该素材的附件路径 */
    private String usageAttachmentPath;

    /** 创建时间，自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 非数据库字段，用于关联查询 */
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
