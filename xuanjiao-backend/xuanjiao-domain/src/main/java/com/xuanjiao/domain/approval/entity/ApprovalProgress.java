package com.xuanjiao.domain.approval.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApprovalProgress {
    private Long id;
    private Long instanceId;
    private Long stageId;
    private String stageName;
    private Integer stageOrder;
    private String status;
    private String approvers;
    private Integer isSubWorkflow;
    private Long parentInstanceId;
    private Long parentTaskId;
    private LocalDateTime approveTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
