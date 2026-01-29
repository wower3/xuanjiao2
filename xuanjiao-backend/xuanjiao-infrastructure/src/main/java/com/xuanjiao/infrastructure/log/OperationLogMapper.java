package com.xuanjiao.infrastructure.log;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuanjiao.infrastructure.dataobject.OperationLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogDO> {
}
