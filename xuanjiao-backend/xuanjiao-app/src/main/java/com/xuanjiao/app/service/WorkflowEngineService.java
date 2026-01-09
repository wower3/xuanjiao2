package com.xuanjiao.app.service;

public interface WorkflowEngineService {
    Long startProcess(Long workflowId, String businessType, Long businessId, Long applicantId);
    void completeTask(Long taskId, Long userId, boolean approved, String comment);
}
