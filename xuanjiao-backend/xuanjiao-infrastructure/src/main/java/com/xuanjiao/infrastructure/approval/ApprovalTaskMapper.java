package com.xuanjiao.infrastructure.approval;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.dataobject.ApprovalTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审批任务数据访问接口
 * <p>定义审批任务的数据库操作方法，对应SQL实现</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.approval.entity.ApprovalTask
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

    /**
     * 重置任务为重新提交状态（status=PENDING, is_first_approver=0, next_stage_approver_ids=null, comment=null, approve_time=null）
     * 用于工作流退回后重新提交场景
     */
    int resetForResubmit(@Param("id") Long id);

    /**
     * 查询用户的流经事项（优化的JOIN查询，避免N+1问题）
     * @param userId 用户ID
     * @param businessType 业务类型筛选
     * @param status 状态筛选
     * @return 流经事项列表
     */
    List<FlowItemDO> selectFlowItemsByUser(@Param("userId") Long userId,
                                           @Param("businessType") String businessType,
                                           @Param("status") String status);
}
