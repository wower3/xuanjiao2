package com.xuanjiao.infrastructure.log;

import lombok.Data;

/**
 * OperationLog查询条件对象
 * 用于查询操作日志
 */
@Data
public class OperationLogQuery {
    private Long id;
    private Long operatorId;
    private String operationType;
    private String targetType;
    private Long targetId;
}
