package com.xuanjiao.infrastructure.usage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.dataobject.UsageLogDO;
import com.xuanjiao.infrastructure.dataobject.UsageLogWithUserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 素材使用日志数据访问接口
 * <p>定义素材使用日志的数据库操作方法，对应SQL实现</p>
 * <p>记录素材的下载、使用等操作行为</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.log.entity.UsageLog
 */
@Mapper
public interface UsageLogMapper {

    /**
     * Select usage log by ID
     */
    UsageLogDO selectById(@Param("id") Long id);

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

    /**
     * 分页查询使用日志（JOIN 用户，避免 N+1）
     *
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<UsageLogWithUserDO> selectPageWithUser(Page<UsageLogWithUserDO> page, @Param("query") UsageLogQuery query);
}
