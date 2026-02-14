package com.xuanjiao.client.approval;

import lombok.Data;

/**
 * 流经事项查询对象
 *
 * <p>用于查询当前用户参与过的所有工单（作为发起人或审批人），
 * 支持按业务类型和状态筛选。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalGetMyFlowItemsQry {

    /**
     * 当前页码（从1开始，默认为1）
     */
    private Integer pageNum = 1;

    /**
     * 每页记录数（默认为10）
     */
    private Integer pageSize = 10;

    /**
     * 业务类型筛选
     * <ul>
     *   <li>MATERIAL_ENTRY - 素材录入申请</li>
     *   <li>ASSET_USAGE - 素材使用申请</li>
     *   <li>ASSET_DELETION - 素材删除申请</li>
     * </ul>
     */
    private String businessType;

    /**
     * 审批状态筛选
     * <ul>
     *   <li>PENDING - 待审批</li>
     *   <li>APPROVED - 已通过</li>
     *   <li>REJECTED - 已驳回</li>
     *   <li>CANCELLED - 已取消</li>
     * </ul>
     */
    private String status;
}
