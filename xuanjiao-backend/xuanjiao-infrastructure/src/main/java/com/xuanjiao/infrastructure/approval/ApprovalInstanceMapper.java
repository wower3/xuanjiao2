package com.xuanjiao.infrastructure.approval;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.dataobject.ApprovalInstanceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审批实例数据访问接口
 *
 * <p>定义审批实例表的数据库操作方法，对应 XML Mapper 实现。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface ApprovalInstanceMapper {

    /**
     * 根据主键查询审批实例
     *
     * @param id 审批实例ID
     * @return 审批实例数据对象
     */
    ApprovalInstanceDO selectById(@Param("id") Long id);

    /**
     * 动态条件查询审批实例列表
     *
     * @param query 查询条件
     * @return 审批实例数据对象列表
     */
    List<ApprovalInstanceDO> selectList(ApprovalInstanceQuery query);

    /**
     * 分页查询审批实例
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    Page<ApprovalInstanceDO> selectPage(@Param("page") Page<ApprovalInstanceDO> page,
                                        @Param("query") ApprovalInstanceQuery query);

    /**
     * 动态条件统计审批实例数量
     *
     * @param query 查询条件
     * @return 数量
     */
    Long selectCount(ApprovalInstanceQuery query);

    /**
     * 插入审批实例
     *
     * @param approvalInstanceDO 审批实例数据对象
     * @return 影响行数
     */
    int insert(ApprovalInstanceDO approvalInstanceDO);

    /**
     * 根据主键更新审批实例
     *
     * @param approvalInstanceDO 审批实例数据对象
     * @return 影响行数
     */
    int updateById(ApprovalInstanceDO approvalInstanceDO);

    /**
     * 查询需要取消的子流程实例
     *
     * <p>用于退回时取消子流程。</p>
     *
     * @param parentInstanceId 父实例ID
     * @param parentTaskIds 父任务ID列表
     * @param includeNullParentTask 是否包含parent_task_id为NULL的实例
     * @return 子流程实例列表
     */
    List<ApprovalInstanceDO> selectSubInstancesToCancel(@Param("parentInstanceId") Long parentInstanceId,
                                                        @Param("parentTaskIds") List<Long> parentTaskIds,
                                                        @Param("includeNullParentTask") boolean includeNullParentTask);

    /**
     * 查询我发起的工单列表（优化的JOIN查询）
     *
     * <p>通过JOIN一次性获取所有需要的数据，避免N+1查询问题。
     * 支持按申请人、业务类型、状态筛选。</p>
     *
     * @param applicantId 申请人ID（单个）
     * @param applicantIds 申请人ID列表（多个）
     * @param businessType 业务类型
     * @param status 审批状态
     * @return 工单列表（包含关联的工作流、申请人、申请单信息）
     */
    List<MyAppliedDO> selectMyAppliedList(@Param("applicantId") Long applicantId,
                                          @Param("applicantIds") List<Long> applicantIds,
                                          @Param("businessType") String businessType,
                                          @Param("status") String status);
}
