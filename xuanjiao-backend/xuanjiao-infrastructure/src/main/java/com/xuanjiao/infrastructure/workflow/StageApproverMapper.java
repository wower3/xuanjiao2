package com.xuanjiao.infrastructure.workflow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.infrastructure.dataobject.StageApproverDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * StageApprover Mapper Interface
 * Refactored from BaseMapper to XML Mapper approach
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
     * Delete stage approver by ID (soft delete)
     */
    int deleteById(@Param("id") Long id);

    /**
     * Batch delete stage approvers by query (soft delete)
     */
    int delete(StageApproverQuery query);

    /**
     * Batch delete stage approvers by query (soft delete)
     * Note: This method maintains compatibility with existing delete(wrapper) calls
     */
    int delete(LambdaQueryWrapper<StageApproverDO> wrapper);
}
