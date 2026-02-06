package com.xuanjiao.app.log;

import com.xuanjiao.domain.log.entity.OperationLog;

/**
 * 操作日志服务接口
 * <p>提供操作日志的记录功能</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.log.impl.OperationLogServiceImpl
 */
public interface OperationLogService {

    /**
     * 记录操作日志
     *
     * @param operatorId 操作人ID
     * @param operatorName 操作人名称
     * @param operationType 操作类型
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param targetName 目标名称
     * @param detail 操作详情
     * @param ipAddress IP地址
     */
    void log(Long operatorId, String operatorName, String operationType,
            String targetType, Long targetId, String targetName, String detail, String ipAddress);
}
