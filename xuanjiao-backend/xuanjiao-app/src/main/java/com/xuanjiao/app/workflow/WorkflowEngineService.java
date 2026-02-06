package com.xuanjiao.app.workflow;

import java.util.List;
import java.util.Map;

/**
 * 工作流引擎服务接口
 * <p>提供工作流的启动、任务完成、子流程管理等核心功能</p>
 * <p>支持主流程和子流程的完整生命周期管理</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.workflow.impl.WorkflowEngineServiceImpl
 */
public interface WorkflowEngineService {

    /**
     * 启动审批流程
     * <p>根据工作流定义创建审批实例和初始任务</p>
     *
     * @param workflowId 工作流ID
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param applicantId 申请人ID
     * @return 创建的审批实例ID
     */
    Long startProcess(Long workflowId, String businessType, Long businessId, Long applicantId);

    /**
     * 完成审批任务
     * <p>处理审批通过或拒绝，更新任务状态和流程进度</p>
     *
     * @param taskId 任务ID
     * @param userId 当前用户ID
     * @param approved 是否通过
     * @param comment 审批意见
     */
    void completeTask(Long taskId, Long userId, boolean approved, String comment);

    /**
     * 退回上一级
     * <p>将任务退回给上一阶段的审批人</p>
     *
     * @param taskId 任务ID
     * @param userId 当前用户ID
     * @param comment 退回原因（可选）
     */
    void returnTask(Long taskId, Long userId, String comment);

    /**
     * 追回工单
     * <p>发起人可以在审批过程中追回正在审批的工单</p>
     *
     * @param instanceId 审批实例ID
     * @param userId 当前用户ID（发起人）
     * @param comment 追回原因（可选）
     */
    void withdrawInstance(Long instanceId, Long userId, String comment);

    /**
     * 重新发起子流程
     * <p>子流程被拒绝后，可以重新发起</p>
     *
     * @param taskId "重新发起子流程"任务ID
     * @param userId 当前用户ID
     * @param approverIds 子流程第一层审批人ID列表
     */
    void restartSubWorkflow(Long taskId, Long userId, List<Long> approverIds);

    /**
     * 为指定阶段启动所有子流程
     * <p>在主流程审批到特定阶段时，自动启动所有配置的子流程</p>
     *
     * @param parentInstanceId 父实例ID
     * @param parentStageId 父阶段ID
     * @param parentTaskId 触发子流程的父任务ID（用于记录关联关系）
     * @param subWorkflowApproverIds 子流程第一层审批人ID映射（子流程ID -> 审批人ID列表）
     */
    void startSubProcessesForStage(Long parentInstanceId, Long parentStageId, Long parentTaskId, Map<Long, List<Long>> subWorkflowApproverIds);
}
