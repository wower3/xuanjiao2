package com.xuanjiao.domain.approval.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApprovalTask {
    private Long id;
    private Long instanceId;
    private Long stageId;
    private Long approverId;
    private String status;
    private String comment;
    private LocalDateTime approveTime;
    private String nextStageApproverIds;
    private String subWorkflowApproverIds;
    private Long selectedByUserId;
    private Integer isFirstApprover;
    private LocalDateTime createTime;
}
