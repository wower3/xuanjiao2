package com.xuanjiao.domain.usage.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材使用申请-素材关联实体
 * <p>使用申请与素材的多对多关系中间表</p>
 * <p>一个使用申请可以包含多个素材，一个素材可以被多个使用申请使用</p>
 * <p>记录每个素材在申请中的具体使用信息（用途、渠道、是否二次创作等）</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO
 */
@Data
public class UsageApplyAsset {
    /** 关联记录唯一标识，自增主键 */
    private Long id;

    /** 关联的使用申请ID，指向UsageApply */
    private Long usageApplyId;

    /** 关联的素材ID，指向Asset */
    private Long assetId;

    /** 该素材的使用说明，描述该素材在此申请中的具体用途 */
    private String usageDescription;

    /** 该素材的发布渠道，覆盖全局发布渠道设置 */
    private String usagePublishChannel;

    /** 该素材是否二次创作：0-否、1-是 */
    private Integer usageIsSecondaryCreation;

    /** 该素材的附件路径 */
    private String usageAttachmentPath;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 关联的素材信息（非数据库字段，用于展示） */
    private String assetName;
    private String assetType;
    private String assetFilePath;
    private String assetThumbnailPath;
    private String assetStatus;
}
