package com.xuanjiao.domain.log.repository;

import com.xuanjiao.domain.log.entity.OperationLog;

/**
 * 操作日志Repository接口
 */
public interface OperationLogRepository {
    /**
     * 保存操作日志
     */
    OperationLog save(OperationLog log);
}
