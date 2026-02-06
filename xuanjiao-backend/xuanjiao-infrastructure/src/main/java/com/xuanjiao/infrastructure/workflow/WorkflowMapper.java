package com.xuanjiao.infrastructure.workflow;

import com.xuanjiao.infrastructure.dataobject.WorkflowDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作流定义数据访问接口
 * <p>定义工作流定义的数据库操作方法，对应SQL实现</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.workflow.entity.Workflow
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
