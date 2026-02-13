package com.xuanjiao.client.dto.usage;

import com.xuanjiao.client.dto.common.BasePageQry;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 素材使用申请查询对象
 *
 * <p>封装素材使用申请列表查询的过滤条件和分页参数。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UsageApplyQry extends BasePageQry {

    /**
     * 素材ID（按素材过滤）
     */
    private Long assetId;

    /**
     * 申请状态（DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已驳回）
     */
    private String status;
}
