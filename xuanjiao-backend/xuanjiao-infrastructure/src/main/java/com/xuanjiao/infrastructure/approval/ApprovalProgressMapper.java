package com.xuanjiao.infrastructure.approval;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xuanjiao.infrastructure.dataobject.ApprovalProgressDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalProgressMapper {

    // ==================== 基础CRUD方法 ====================

    /**
     * 根据ID查询进度
     */
    ApprovalProgressDO selectById(@Param("id") Long id);

    /**
     * 根据查询条件查询单个进度
     */
    ApprovalProgressDO selectOne(ApprovalProgressQuery query);

    /**
     * 根据查询条件查询进度列表
     */
    List<ApprovalProgressDO> selectList(ApprovalProgressQuery query);

    /**
     * 根据查询条件统计数量
     */
    Long selectCount(ApprovalProgressQuery query);

    /**
     * 插入进度
     */
    int insert(ApprovalProgressDO approvalProgressDO);

    /**
     * 根据ID更新进度
     */
    int updateById(ApprovalProgressDO approvalProgressDO);

    /**
     * 使用UpdateWrapper更新（用于强制设置字段为null）
     * 保留此方法用于LambdaUpdateWrapper场景（如强制设置字段为null）
     */
    int update(@Param("entity") ApprovalProgressDO entity, @Param("ew") LambdaUpdateWrapper<ApprovalProgressDO> wrapper);

    // ==================== 自定义查询方法 ====================

    /**
     * 根据实例ID查询进度列表
     */
    List<ApprovalProgressDO> selectByInstanceId(@Param("instanceId") Long instanceId);

    /**
     * 根据父实例ID查询所有子流程进度
     */
    List<ApprovalProgressDO> selectByParentInstanceId(@Param("parentInstanceId") Long parentInstanceId);
}
