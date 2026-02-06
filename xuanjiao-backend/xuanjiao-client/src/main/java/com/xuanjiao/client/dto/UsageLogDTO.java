package com.xuanjiao.client.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsageLogDTO {
    private Long id;
    private Long assetId;
    private Long userId;
    private String username;
    private String action;
    private String ip;
    private String deptName;
    private String usageDescription;
    private String usagePublishChannel;
    private LocalDateTime createTime;
}
