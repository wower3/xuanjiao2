package com.xuanjiao.domain.workflow.entity;

import lombok.Data;
import java.util.List;

@Data
public class WorkflowStage {
    private Long id;
    private Long workflowId;
    private String name;
    private Integer stageOrder;
    private String approveType;
    private List<StageApprover> approvers;
}
