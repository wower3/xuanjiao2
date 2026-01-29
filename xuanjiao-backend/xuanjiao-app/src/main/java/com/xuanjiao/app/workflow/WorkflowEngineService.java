package com.xuanjiao.app.workflow;

import java.util.List;
import java.util.Map;

public interface WorkflowEngineService {
    Long startProcess(Long workflowId, String businessType, Long businessId, Long applicantId);
    void completeTask(Long taskId, Long userId, boolean approved, String comment);

    /**
     * 退回上一级
     * @param taskId 任务ID
     * @param userId 当前用户ID
     * @param comment 退回原因（可选）
     */
    void returnTask(Long taskId, Long userId, String comment);

    /**
     * 追回工单（发起人追回正在审批的工单）
     * @param instanceId 审批实例ID
     * @param userId 当前用户ID（发起人）
     * @param comment 追回原因（可选）
     */
    void withdrawInstance(Long instanceId, Long userId, String comment);

    /**
     * 重新发起子流程
     * @param taskId "重新发起子流程"任务ID
     * @param userId 当前用户ID
     * @param approverIds 子流程第一层审批人ID列表
     */
    void restartSubWorkflow(Long taskId, Long userId, List<Long> approverIds);

    /**
     * 为指定阶段启动所有子流程
     * @param parentInstanceId 父实例ID
     * @param parentStageId 父阶段ID
     * @param parentTaskId 触发子流程的父任务ID（用于记录关联关系）
     * @param subWorkflowApproverIds 子流程第一层审批人ID映射（子流程ID -> 审批人ID列表）
     */
    void startSubProcessesForStage(Long parentInstanceId, Long parentStageId, Long parentTaskId, Map<Long, List<Long>> subWorkflowApproverIds);
}
