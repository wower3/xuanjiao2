package com.xuanjiao.infrastructure.log.repository;

import com.xuanjiao.domain.log.entity.OperationLog;
import com.xuanjiao.domain.log.repository.OperationLogRepository;
import com.xuanjiao.infrastructure.dataobject.OperationLogDO;
import com.xuanjiao.infrastructure.log.OperationLogMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class OperationLogRepositoryImpl implements OperationLogRepository {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public OperationLog save(OperationLog log) {
        OperationLogDO logDO = new OperationLogDO();
        BeanUtils.copyProperties(log, logDO);
        operationLogMapper.insert(logDO);
        log.setId(logDO.getId());
        return log;
    }
}
