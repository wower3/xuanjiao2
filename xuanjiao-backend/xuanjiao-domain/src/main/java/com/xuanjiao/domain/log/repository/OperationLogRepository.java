package com.xuanjiao.domain.log.repository;

import com.xuanjiao.domain.log.entity.OperationLog;

/**
 * 操作日志仓储接口
 *
 * <p>定义操作日志的持久化操作。</p>
 * <p>操作日志用于记录用户在系统中的关键操作，用于审计和追溯。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface OperationLogRepository {

    /**
     * 保存操作日志
     *
     * <p>将操作日志持久化到数据库。</p>
     *
     * @param log 操作日志实体
     * @return 保存后的操作日志
     */
    OperationLog save(OperationLog log);
}
