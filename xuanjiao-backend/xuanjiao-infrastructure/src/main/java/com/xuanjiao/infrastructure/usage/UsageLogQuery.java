package com.xuanjiao.infrastructure.usage;

import lombok.Data;

/**
 * UsageLog Query Object
 * Dynamic query parameters for UsageLogMapper
 */
@Data
public class UsageLogQuery {
    private Long id;
    private Long assetId;
    private Long userId;
    private String action;
    private String ip;
    private String deptName;
    private String orderByField;
    private String orderByDirection;
}
