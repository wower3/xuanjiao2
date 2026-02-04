package com.xuanjiao.infrastructure.approval;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.dataobject.ApprovalTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审批任务Mapper
 */
@Mapper
public interface ApprovalTaskMapper {

    // ==================== 基础CRUD方法 ====================

    /**
     * 根据ID查询任务
     */
    ApprovalTaskDO selectById(@Param("id") Long id);

    /**
     * 根据查询条件查询单个任务
     */
    ApprovalTaskDO selectOne(ApprovalTaskQuery query);

    /**
     * 根据查询条件查询任务列表
     */
    List<ApprovalTaskDO> selectList(ApprovalTaskQuery query);

    /**
     * 根据查询条件统计数量
     */
    Long selectCount(ApprovalTaskQuery query);

    /**
     * 分页查询任务列表
     */
    IPage<ApprovalTaskDO> selectPage(@Param("page") Page<ApprovalTaskDO> page, @Param("query") ApprovalTaskQuery query);

    /**
     * 插入任务
     */
    int insert(ApprovalTaskDO approvalTaskDO);

    /**
     * 根据ID更新任务
     */
    int updateById(ApprovalTaskDO approvalTaskDO);

    /**
     * 使用UpdateWrapper更新（用于强制设置字段为null）
     * 保留此方法用于LambdaUpdateWrapper场景（如强制设置字段为null）
     */
    int update(@Param("entity") ApprovalTaskDO entity, @Param("ew") com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ApprovalTaskDO> wrapper);
}
