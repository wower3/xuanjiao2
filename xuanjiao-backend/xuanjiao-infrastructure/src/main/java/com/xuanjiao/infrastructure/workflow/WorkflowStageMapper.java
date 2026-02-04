package com.xuanjiao.infrastructure.workflow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.infrastructure.dataobject.WorkflowStageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * WorkflowStage Mapper Interface
 * Refactored from BaseMapper to XML Mapper approach
 */
@Mapper
public interface WorkflowStageMapper {

    /**
     * Select workflow stage by ID
     */
    WorkflowStageDO selectById(@Param("id") Long id);

    /**
     * Select workflow stages with dynamic query conditions
     */
    List<WorkflowStageDO> selectList(WorkflowStageQuery query);

    /**
     * Count workflow stages with dynamic query conditions
     */
    Long selectCount(WorkflowStageQuery query);

    /**
     * Insert new workflow stage
     */
    int insert(WorkflowStageDO workflowStageDO);

    /**
     * Update workflow stage by ID
     */
    int updateById(WorkflowStageDO workflowStageDO);

    /**
     * Delete workflow stage by ID (soft delete)
     */
    int deleteById(@Param("id") Long id);

    /**
     * Batch delete workflow stages by query (soft delete)
     * Note: This method maintains compatibility with existing delete(wrapper) calls
     */
    int delete(LambdaQueryWrapper<WorkflowStageDO> wrapper);
}
