package com.xuanjiao.infrastructure.approval;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.dataobject.ApprovalInstanceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审批实例数据访问接口
 * <p>定义审批实例的数据库操作方法，对应SQL实现</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.approval.entity.ApprovalInstance
 */
@Mapper
public interface ApprovalInstanceMapper {

    // ==================== 基础CRUD方法 ====================

    /**
     * 根据ID查询实例
     */
    ApprovalInstanceDO selectById(@Param("id") Long id);

    /**
     * 根据查询条件查询实例列表
     */
    List<ApprovalInstanceDO> selectList(ApprovalInstanceQuery query);

    /**
     * 根据查询条件分页查询实例列表
     */
    Page<ApprovalInstanceDO> selectPage(@Param("page") Page<ApprovalInstanceDO> page,
                                        @Param("query") ApprovalInstanceQuery query);

    /**
     * 根据查询条件统计数量
     */
    Long selectCount(ApprovalInstanceQuery query);

    /**
     * 插入实例
     */
    int insert(ApprovalInstanceDO approvalInstanceDO);

    /**
     * 根据ID更新实例
     */
    int updateById(ApprovalInstanceDO approvalInstanceDO);

    // ==================== 自定义查询方法 ====================

    /**
     * 查询需要取消的子流程实例（用于退回时取消子流程）
     * @param parentInstanceId 父实例ID
     * @param parentTaskIds 父任务ID列表
     * @param includeNullParentTask 是否包含parent_task_id为NULL的实例
     */
    List<ApprovalInstanceDO> selectSubInstancesToCancel(@Param("parentInstanceId") Long parentInstanceId,
                                                        @Param("parentTaskIds") List<Long> parentTaskIds,
                                                        @Param("includeNullParentTask") boolean includeNullParentTask);
}
