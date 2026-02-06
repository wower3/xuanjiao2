package com.xuanjiao.infrastructure.workflow;

import com.xuanjiao.infrastructure.dataobject.StageApproverDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 阶段审批人数据访问接口
 * <p>定义阶段审批人的数据库操作方法，对应SQL实现</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.workflow.entity.StageApprover
 */
@Mapper
public interface StageApproverMapper {

    /**
     * Select stage approver by ID
     */
    StageApproverDO selectById(@Param("id") Long id);

    /**
     * Select stage approvers with dynamic query conditions
     */
    List<StageApproverDO> selectList(StageApproverQuery query);

    /**
     * Count stage approvers with dynamic query conditions
     */
    Long selectCount(StageApproverQuery query);

    /**
     * Insert new stage approver
     */
    int insert(StageApproverDO stageApproverDO);

    /**
     * Update stage approver by ID
     */
    int updateById(StageApproverDO stageApproverDO);

    /**
     * Delete stage approver by ID (hard delete)
     */
    int deleteById(@Param("id") Long id);

    /**
     * Batch delete stage approvers by query (hard delete)
     */
    int delete(StageApproverQuery query);
}
