package com.xuanjiao.infrastructure.log;

import com.xuanjiao.infrastructure.dataobject.OperationLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志数据访问接口
 * <p>定义操作日志的数据库操作方法，对应SQL实现</p>
 * <p>记录用户的关键操作行为，支持审计和追溯</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.log.entity.OperationLog
 */
@Mapper
public interface OperationLogMapper {

    // ==================== 基础CRUD方法 ====================

    /**
     * 根据ID查询日志
     */
    OperationLogDO selectById(@Param("id") Long id);

    /**
     * 根据查询条件查询单个日志
     */
    OperationLogDO selectOne(OperationLogQuery query);

    /**
     * 根据查询条件查询日志列表
     */
    List<OperationLogDO> selectList(OperationLogQuery query);

    /**
     * 根据查询条件统计数量
     */
    Long selectCount(OperationLogQuery query);

    /**
     * 插入日志
     */
    int insert(OperationLogDO operationLogDO);

    /**
     * 根据ID更新日志
     */
    int updateById(OperationLogDO operationLogDO);
}
