package com.xuanjiao.infrastructure.approval;

import lombok.Data;

import java.util.List;

/**
 * 审批实例查询条件对象
 *
 * <p>用于动态构建审批实例查询条件，对应 ApprovalInstanceMapper 使用。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalInstanceQuery {

    /**
     * 审批实例ID
     */
    private Long id;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 业务类型（MATERIAL_ENTRY, ASSET_USAGE, ASSET_DELETION）
     */
    private String businessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人ID列表（IN查询）
     */
    private List<Long> applicantIds;

    /**
     * 当前阶段ID
     */
    private Long currentStageId;

    /**
     * 审批状态（PENDING, APPROVED, REJECTED, CANCELLED）
     */
    private String status;

    /**
     * 父实例ID（用于子流程）
     */
    private Long parentInstanceId;

    /**
     * 父实例ID是否为空（用于 IS NULL 查询，查询主流程）
     */
    private Boolean parentInstanceIdIsNull;

    /**
     * 父任务ID
     */
    private Long parentTaskId;

    /**
     * 根实例ID
     */
    private Long rootInstanceId;

    /**
     * 子流程审批人IDs（JSON格式）
     */
    private String subWorkflowApproverIds;

    /**
     * 排除的状态（用于 != 查询）
     */
    private String statusNotEqual;

    /**
     * 包含的状态列表（用于 IN 查询）
     */
    private String statusIn;
}
