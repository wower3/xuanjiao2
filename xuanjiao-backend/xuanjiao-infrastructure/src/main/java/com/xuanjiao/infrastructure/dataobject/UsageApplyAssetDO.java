package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材使用申请-素材关联数据对象
 *
 * <p>映射数据库 usage_apply_asset 表，用于 MyBatis 数据访问。</p>
 * <p>存储使用申请与素材的关联关系，支持多对多关系，每个关联可包含独立的素材配置。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("usage_apply_asset")
public class UsageApplyAssetDO {

    /**
     * 关联ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的使用申请ID
     */
    private Long usageApplyId;

    /**
     * 关联的素材ID
     */
    private Long assetId;

    /**
     * 该素材的使用说明
     */
    private String usageDescription;

    /**
     * 该素材的发布渠道
     */
    private String usagePublishChannel;

    /**
     * 该素材是否二次创作：0-否、1-是
     */
    private Integer usageIsSecondaryCreation;

    /**
     * 该素材的附件路径
     */
    private String usageAttachmentPath;

    /**
     * 创建时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 素材名称（非数据库字段，用于关联查询）
     */
    @TableField(exist = false)
    private String assetName;

    /**
     * 素材类型（非数据库字段，用于关联查询）
     */
    @TableField(exist = false)
    private String assetType;

    /**
     * 素材文件路径（非数据库字段，用于关联查询）
     */
    @TableField(exist = false)
    private String assetFilePath;

    /**
     * 素材缩略图路径（非数据库字段，用于关联查询）
     */
    @TableField(exist = false)
    private String assetThumbnailPath;

    /**
     * 素材状态（非数据库字段，用于关联查询）
     */
    @TableField(exist = false)
    private String assetStatus;
}
