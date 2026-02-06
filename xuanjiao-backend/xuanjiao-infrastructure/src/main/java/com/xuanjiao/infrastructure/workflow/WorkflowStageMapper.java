package com.xuanjiao.infrastructure.workflow;

import com.xuanjiao.infrastructure.dataobject.WorkflowStageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作流阶段数据访问接口
 * <p>定义工作流阶段的数据库操作方法，对应SQL实现</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.workflow.entity.WorkflowStage
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
     * Batch delete workflow stages by WorkflowStageQuery (soft delete)
     */
    int delete(WorkflowStageQuery query);
}
