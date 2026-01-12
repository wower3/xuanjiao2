package com.xuanjiao.domain.usage.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsageApply {
    private Long id;
    private Long assetId;
    private Long userId;
    private String purpose;
    private String scope;
    private Long workflowId;
    private String status;
    private Long approvalInstanceId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
