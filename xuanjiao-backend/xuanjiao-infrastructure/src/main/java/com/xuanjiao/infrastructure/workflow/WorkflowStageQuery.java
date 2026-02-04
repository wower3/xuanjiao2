package com.xuanjiao.infrastructure.workflow;

import lombok.Data;

/**
 * WorkflowStage Query Object
 * Dynamic query parameters for WorkflowStageMapper
 */
@Data
public class WorkflowStageQuery {
    private Long id;
    private Long workflowId;
    private String name;
    private Integer stageOrder;
    private String approveType;
    private Integer deleted;
    private String orderByField;
    private String orderByDirection;
}
