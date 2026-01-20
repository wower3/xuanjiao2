package com.xuanjiao.app.workflow;

import java.util.List;
import java.util.Map;

public interface WorkflowEngineService {
    Long startProcess(Long workflowId, String businessType, Long businessId, Long applicantId);
    void completeTask(Long taskId, Long userId, boolean approved, String comment);

    /**
     * 为指定阶段启动所有子流程
     * @param parentInstanceId 父实例ID
     * @param parentStageId 父阶段ID
     * @param parentTaskId 触发子流程的父任务ID（用于记录关联关系）
     * @param subWorkflowApproverIds 子流程第一层审批人ID映射（子流程ID -> 审批人ID列表）
     */
    void startSubProcessesForStage(Long parentInstanceId, Long parentStageId, Long parentTaskId, Map<Long, List<Long>> subWorkflowApproverIds);
}
