package com.xuanjiao.client;

import lombok.Data;

/**
 * 素材使用申请查询命令
 *
 * <p>封装素材使用申请列表查询的过滤条件和分页参数。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UsageApplyQueryCmd {

    /**
     * 素材ID（按素材过滤）
     */
    private Long assetId;

    /**
     * 申请状态（DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已驳回）
     */
    private String status;

    /**
     * 当前页码（从1开始，默认为1）
     */
    private Integer pageNum = 1;

    /**
     * 每页记录数（默认为10）
     */
    private Integer pageSize = 10;
}
