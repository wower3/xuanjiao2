package com.xuanjiao.client.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorkflowStageDTO {
    private Long id;
    private Long workflowId; // 流程ID
    private String name;
    private Integer stageOrder;
    private String approveType;
    private List<StageApproverDTO> approvers;
}
