package com.xuanjiao.infrastructure.workflow;

import lombok.Data;

/**
 * StageApprover Query Object
 * Dynamic query parameters for StageApproverMapper
 */
@Data
public class StageApproverQuery {
    private Long id;
    private Long stageId;
    private String approverType;
    private Long approverId;
    private Integer checkSecondaryDept;
    private Long subWorkflowId;
    private Boolean subWorkflowIdNull; // For IS NULL query
    private Boolean subWorkflowIdNotNull; // For IS NOT NULL query
    private String orderByField;
    private String orderByDirection;
}
