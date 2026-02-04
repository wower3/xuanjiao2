package com.xuanjiao.infrastructure.workflow;

import com.xuanjiao.infrastructure.dataobject.WorkflowDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Workflow Mapper Interface
 * Refactored from BaseMapper to XML Mapper approach
 */
@Mapper
public interface WorkflowMapper {

    /**
     * Select workflow by ID
     */
    WorkflowDO selectById(@Param("id") Long id);

    /**
     * Select workflows with dynamic query conditions
     */
    List<WorkflowDO> selectList(WorkflowQuery query);

    /**
     * Count workflows with dynamic query conditions
     */
    Long selectCount(WorkflowQuery query);

    /**
     * Insert new workflow
     */
    int insert(WorkflowDO workflowDO);

    /**
     * Update workflow by ID
     */
    int updateById(WorkflowDO workflowDO);

    /**
     * Delete workflow by ID (soft delete)
     */
    int deleteById(@Param("id") Long id);
}
