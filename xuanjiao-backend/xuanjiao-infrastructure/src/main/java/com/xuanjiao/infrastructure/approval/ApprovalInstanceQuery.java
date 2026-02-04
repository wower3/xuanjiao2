package com.xuanjiao.infrastructure.approval;

import lombok.Data;

import java.util.List;

/**
 * ApprovalInstance查询条件对象
 * 用于查询审批实例
 */
@Data
public class ApprovalInstanceQuery {
    private Long id;
    private Long workflowId;
    private String businessType;
    private Long businessId;
    private Long applicantId;
    private List<Long> applicantIds;  // 支持多个申请人ID的IN查询
    private Long currentStageId;
    private String status;
    private Long parentInstanceId;
    private Boolean parentInstanceIdIsNull;  // 用于 IS NULL 查询（查询主流程）
    private Long parentTaskId;
    private Long rootInstanceId;
    private String subWorkflowApproverIds;

    // 用于status != 查询
    private String statusNotEqual;

    // 用于多个status查询
    private String statusIn;
}
