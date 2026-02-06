package com.xuanjiao.app.log.impl;

import com.xuanjiao.app.log.OperationLogService;
import com.xuanjiao.domain.log.entity.OperationLog;
import com.xuanjiao.domain.log.repository.OperationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现类
 * <p>实现OperationLogService接口，封装操作日志记录逻辑</p>
 * <p>核心功能：记录用户操作日志（登录、增删改等）</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.log.OperationLogService
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogServiceImpl.class);

    @Autowired
    private OperationLogRepository operationLogRepository;

    @Override
    public void log(Long operatorId, String operatorName, String operationType,
                     String targetType, Long targetId, String targetName, String detail, String ipAddress) {
        try {
            OperationLog log = new OperationLog();
            log.setOperatorId(operatorId);
            log.setOperatorName(operatorName);
            log.setOperationType(operationType);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setTargetName(targetName);
            log.setOperationDetail(detail);
            log.setIpAddress(ipAddress);

            operationLogRepository.save(log);

            logger.info("记录操作日志: operator={}, operation={}, target={}:{}",
                operatorName, operationType, targetType, targetName, targetId);
        } catch (Exception e) {
            logger.error("记录操作日志失败: {}", e.getMessage(), e);
        }
    }
}
