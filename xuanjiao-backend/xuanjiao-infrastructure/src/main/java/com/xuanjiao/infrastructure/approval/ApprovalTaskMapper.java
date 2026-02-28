package com.xuanjiao.infrastructure.approval;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.dataobject.ApprovalTaskDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import com.xuanjiao.infrastructure.approval.PendingTaskItemDO;

/**
 * 审批任务数据访问接口
 *
 * <p>定义审批任务表的数据库操作方法，对应 XML Mapper 实现。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface ApprovalTaskMapper {

    /**
     * 根据主键查询审批任务
     *
     * @param id 任务ID
     * @return 审批任务数据对象
     */
    ApprovalTaskDO selectById(@Param("id") Long id);

    /**
     * 根据查询条件查询单个审批任务
     *
     * @param query 查询条件
     * @return 审批任务数据对象
     */
    ApprovalTaskDO selectOne(ApprovalTaskQuery query);

    /**
     * 动态条件查询审批任务列表
     *
     * @param query 查询条件
     * @return 审批任务数据对象列表
     */
    List<ApprovalTaskDO> selectList(ApprovalTaskQuery query);

    /**
     * 动态条件统计审批任务数量
     *
     * @param query 查询条件
     * @return 数量
     */
    Long selectCount(ApprovalTaskQuery query);

    /**
     * 分页查询审批任务
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<ApprovalTaskDO> selectPage(@Param("page") Page<ApprovalTaskDO> page, @Param("query") ApprovalTaskQuery query);

    /**
     * 插入审批任务
     *
     * @param approvalTaskDO 审批任务数据对象
     * @return 影响行数
     */
    int insert(ApprovalTaskDO approvalTaskDO);

    /**
     * 根据主键更新审批任务
     *
     * @param approvalTaskDO 审批任务数据对象
     * @return 影响行数
     */
    int updateById(ApprovalTaskDO approvalTaskDO);

    /**
     * 使用 UpdateWrapper 更新（用于强制设置字段为null）
     *
     * <p>保留此方法用于 LambdaUpdateWrapper 场景（如强制设置字段为null）。</p>
     *
     * @param entity 实体对象
     * @param wrapper 更新条件包装器
     * @return 影响行数
     */
    int update(@Param("entity") ApprovalTaskDO entity, @Param("ew") LambdaUpdateWrapper<ApprovalTaskDO> wrapper);

    /**
     * 重置任务为重新提交状态
     *
     * <p>用于工作流退回后重新提交场景。</p>
     * <p>将 status 设为 PENDING，is_first_approver 设为 0，清空 next_stage_approver_ids、comment、approve_time。</p>
     *
     * @param id 任务ID
     * @return 影响行数
     */
    int resetForResubmit(@Param("id") Long id);

    /**
     * 查询用户的流经事项
     *
     * <p>优化的 JOIN 查询，避免 N+1 问题。</p>
     *
     * @param userId 用户ID
     * @param businessType 业务类型筛选（MATERIAL_ENTRY, ASSET_USAGE, ASSET_DELETION）
     * @param status 状态筛选
     * @return 流经事项列表
     */
    List<FlowItemDO> selectFlowItemsByUser(@Param("userId") Long userId,
                                           @Param("businessType") String businessType,
                                           @Param("status") String status);

    /**
     * 分页查询待办任务（带JOIN，避免N+1问题）
     *
     * <p>一次性获取任务、实例、工作流、申请人、业务申请等信息。</p>
     *
     * @param userId 用户ID
     * @param businessType 业务类型筛选（MATERIAL_ENTRY, ASSET_USAGE, ASSET_DELETION）
     * @param offset 偏移量
     * @param pageSize 每页大小
     * @return 待办任务列表
     */
    List<PendingTaskItemDO> selectPendingTaskPage(@Param("userId") Long userId,
                                                   @Param("businessType") String businessType,
                                                   @Param("offset") Integer offset,
                                                   @Param("pageSize") Integer pageSize);

    /**
     * 查询待办任务数量
     *
     * @param userId 用户ID
     * @param businessType 业务类型筛选
     * @return 待办任务数量
     */
    Long selectPendingTaskCount(@Param("userId") Long userId,
                                @Param("businessType") String businessType);
}
