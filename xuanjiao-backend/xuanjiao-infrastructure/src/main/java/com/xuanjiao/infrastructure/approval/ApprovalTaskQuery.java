package com.xuanjiao.infrastructure.approval;

import lombok.Data;

import java.util.List;

/**
 * 审批任务查询条件对象
 *
 * <p>用于动态构建审批任务查询条件，对应 ApprovalTaskMapper 使用。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalTaskQuery {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 审批实例ID
     */
    private Long instanceId;

    /**
     * 阶段ID
     */
    private Long stageId;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 任务状态（PENDING, APPROVED, REJECTED, RETURNED, CANCELLED）
     */
    private String status;

    /**
     * 是否是第一个审批人（0-否、1-是）
     */
    private Integer isFirstApprover;

    /**
     * 任务类型（NORMAL-普通审批、RESTART_SUB_WORKFLOW-重新发起子流程）
     */
    private String taskType;

    /**
     * 子流程审批人IDs是否不为空（IS NOT NULL 查询）
     */
    private Boolean subWorkflowApproverIdsNotNull;

    /**
     * 状态列表（IN 查询）
     */
    private List<String> statusIn;

    /**
     * 排除的任务ID（!= 查询）
     */
    private Long idNotEqual;

    /**
     * 业务类型（需关联 approval_instance 表查询）
     */
    private String businessType;
}
