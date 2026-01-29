package com.xuanjiao.domain.approval.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApprovalInstance {
    private Long id;
    private Long workflowId;
    private String businessType;
    private Long businessId;
    private Long applicantId;
    private Long currentStageId;
    private String status;
    private Long parentInstanceId;
    private Long parentTaskId;
    private Long rootInstanceId;
    private String subWorkflowApproverIds;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
