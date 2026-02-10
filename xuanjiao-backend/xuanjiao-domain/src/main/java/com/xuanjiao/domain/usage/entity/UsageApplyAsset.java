package com.xuanjiao.domain.usage.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材使用申请-素材关联实体
 *
 * <p>使用申请与素材的多对多关系中间表。</p>
 * <p>一个使用申请可以包含多个素材，一个素材可以被多个使用申请使用。</p>
 * <p>记录每个素材在申请中的具体使用信息（用途、渠道、是否二次创作等）。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UsageApplyAsset {

    /**
     * 关联记录唯一标识
     *
     * <p>自增主键。</p>
     */
    private Long id;

    /**
     * 关联的使用申请ID
     *
     * <p>指向UsageApply。</p>
     */
    private Long usageApplyId;

    /**
     * 关联的素材ID
     *
     * <p>指向Asset。</p>
     */
    private Long assetId;

    /**
     * 该素材的使用说明
     *
     * <p>描述该素材在此申请中的具体用途。</p>
     */
    private String usageDescription;

    /**
     * 该素材的发布渠道
     *
     * <p>覆盖全局发布渠道设置。</p>
     */
    private String usagePublishChannel;

    /**
     * 该素材是否二次创作
     *
     * <p>0-否、1-是。</p>
     */
    private Integer usageIsSecondaryCreation;

    /**
     * 该素材的附件路径
     */
    private String usageAttachmentPath;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 关联的素材名称
     *
     * <p>非数据库字段，用于展示。</p>
     */
    private String assetName;

    /**
     * 关联的素材类型
     *
     * <p>非数据库字段，用于展示。</p>
     */
    private String assetType;

    /**
     * 关联的素材文件路径
     *
     * <p>非数据库字段，用于展示。</p>
     */
    private String assetFilePath;

    /**
     * 关联的素材缩略图路径
     *
     * <p>非数据库字段，用于展示。</p>
     */
    private String assetThumbnailPath;

    /**
     * 关联的素材状态
     *
     * <p>非数据库字段，用于展示。</p>
     */
    private String assetStatus;
}
