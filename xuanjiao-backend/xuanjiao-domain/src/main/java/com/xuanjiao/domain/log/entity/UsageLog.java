package com.xuanjiao.domain.log.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsageLog {
    private Long id;
    private Long assetId;
    private Long userId;
    private String action;
    private String detail;
    private String ip;
    private String deptName;
    private String usageDescription;
    private String usagePublishChannel;
    private LocalDateTime createTime;
}
