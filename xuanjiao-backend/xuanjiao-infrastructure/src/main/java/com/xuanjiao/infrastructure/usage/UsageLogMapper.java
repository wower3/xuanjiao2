package com.xuanjiao.infrastructure.usage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.dataobject.UsageLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * UsageLog Mapper Interface
 * Refactored from BaseMapper to XML Mapper approach
 */
@Mapper
public interface UsageLogMapper {

    /**
     * Select usage log by ID
     */
    UsageLogDO selectById(@Param("id") Long id);

    /**
     * Select usage logs with dynamic query conditions
     */
    List<UsageLogDO> selectList(UsageLogQuery query);

    /**
     * Count usage logs with dynamic query conditions
     */
    Long selectCount(UsageLogQuery query);

    /**
     * Select usage logs with pagination
     */
    Page<UsageLogDO> selectPage(Page<UsageLogDO> page, @Param("query") UsageLogQuery query);

    /**
     * Insert new usage log
     */
    int insert(UsageLogDO usageLogDO);

    /**
     * Update usage log by ID
     */
    int updateById(UsageLogDO usageLogDO);

    /**
     * Delete usage log by ID
     */
    int deleteById(@Param("id") Long id);
}
