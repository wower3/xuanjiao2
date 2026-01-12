package com.xuanjiao.client.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsageApplyDTO {
    private Long id;
    private Long assetId;
    private String assetName;
    private Long userId;
    private String userName;
    private String purpose;
    private String scope;
    private Long workflowId;
    private String workflowName;
    private String status;
    private Long approvalInstanceId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
