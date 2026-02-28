package com.xuanjiao.infrastructure.approval;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuanjiao.infrastructure.dataobject.ApprovalProgressDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审批进度数据访问接口
 *
 * <p>定义审批进度表的数据库操作方法，对应 XML Mapper 实现。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface ApprovalProgressMapper {

    /**
     * 根据主键查询审批进度
     *
     * @param id 进度ID
     * @return 审批进度数据对象
     */
    ApprovalProgressDO selectById(@Param("id") Long id);

    /**
     * 根据查询条件查询单个审批进度
     *
     * @param query 查询条件
     * @return 审批进度数据对象
     */
    ApprovalProgressDO selectOne(ApprovalProgressQuery query);

    /**
     * 动态条件查询审批进度列表
     *
     * @param query 查询条件
     * @return 审批进度数据对象列表
     */
    List<ApprovalProgressDO> selectList(ApprovalProgressQuery query);

    /**
     * 动态条件统计审批进度数量
     *
     * @param query 查询条件
     * @return 数量
     */
    Long selectCount(ApprovalProgressQuery query);

    /**
     * 插入审批进度
     *
     * @param approvalProgressDO 审批进度数据对象
     * @return 影响行数
     */
    int insert(ApprovalProgressDO approvalProgressDO);

    /**
     * 根据主键更新审批进度
     *
     * @param approvalProgressDO 审批进度数据对象
     * @return 影响行数
     */
    int updateById(ApprovalProgressDO approvalProgressDO);

    /**
     * 使用 UpdateWrapper 更新（用于强制设置字段为null）
     *
     * <p>保留此方法用于 LambdaUpdateWrapper 场景（如强制设置字段为null）。</p>
     *
     * @param entity 实体对象
     * @param wrapper 更新条件包装器
     * @return 影响行数
     */
    int update(@Param("entity") ApprovalProgressDO entity, @Param("ew") LambdaUpdateWrapper<ApprovalProgressDO> wrapper);

    /**
     * 根据实例ID查询进度列表
     *
     * @param instanceId 审批实例ID
     * @return 审批进度数据对象列表
     */
    List<ApprovalProgressDO> selectByInstanceId(@Param("instanceId") Long instanceId);

    /**
     * 根据父实例ID查询所有子流程进度
     *
     * @param parentInstanceId 父实例ID
     * @return 子流程进度列表
     */
    List<ApprovalProgressDO> selectByParentInstanceId(@Param("parentInstanceId") Long parentInstanceId);

    /**
     * 重置进度记录为重新提交状态
     *
     * <p>用于工作流退回后重新提交场景。</p>
     * <p>将 status 设为 PENDING，清空 approvers、approve_time。</p>
     *
     * @param id 进度ID
     * @return 影响行数
     */
    int resetForResubmit(@Param("id") Long id);
}
