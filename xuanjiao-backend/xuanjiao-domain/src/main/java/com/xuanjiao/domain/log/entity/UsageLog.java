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
    private LocalDateTime createTime;
}
