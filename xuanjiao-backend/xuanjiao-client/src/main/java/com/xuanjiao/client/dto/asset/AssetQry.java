package com.xuanjiao.client.dto.asset;

import com.xuanjiao.client.dto.common.BasePageQry;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 素材查询对象
 *
 * <p>封装素材列表查询的过滤条件和分页参数。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AssetQry extends BasePageQry {

    /**
     * 素材名称（模糊查询）
     */
    private String name;

    /**
     * 素材类型（IMAGE-图片、VIDEO-视频、DOCUMENT-文档）
     */
    private String type;

    /**
     * 素材状态（DRAFT-草稿、PENDING-待审批、APPROVED-已通过、DELETED-已删除）
     */
    private String status;
}
