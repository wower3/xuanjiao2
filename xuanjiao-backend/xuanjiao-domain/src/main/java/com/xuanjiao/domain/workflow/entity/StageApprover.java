package com.xuanjiao.domain.workflow.entity;

import lombok.Data;

@Data
public class StageApprover {
    private Long id;
    private Long stageId;
    private String approverType;
    private Long approverId;
}
