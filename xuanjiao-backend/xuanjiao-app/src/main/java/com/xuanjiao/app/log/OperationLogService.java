package com.xuanjiao.app.log;

import com.xuanjiao.domain.log.entity.OperationLog;

/**
 * 操作日志Service接口
 */
public interface OperationLogService {
    /**
     * 记录操作日志
     */
    void log(Long operatorId, String operatorName, String operationType,
            String targetType, Long targetId, String targetName, String detail, String ipAddress);
}
