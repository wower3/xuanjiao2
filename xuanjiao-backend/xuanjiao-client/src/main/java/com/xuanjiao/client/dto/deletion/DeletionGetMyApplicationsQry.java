package com.xuanjiao.client.dto.deletion;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 获取我的删除申请列表查询对象
 *
 * <p>用于查询当前用户发起的素材删除申请列表，支持按标题和状态筛选。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class DeletionGetMyApplicationsQry {

    /**
     * 申请标题（模糊查询）
     */
    private String title;

    /**
     * 申请状态（DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已驳回）
     */
    private String status;

    /**
     * 当前页码（从1开始，默认为1）
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页记录数（默认为10）
     */
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;
}
