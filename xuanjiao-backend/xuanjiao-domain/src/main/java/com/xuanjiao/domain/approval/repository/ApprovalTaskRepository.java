package com.xuanjiao.domain.approval.repository;

import com.xuanjiao.domain.approval.entity.ApprovalTask;
import java.util.List;

public interface ApprovalTaskRepository {
    ApprovalTask findById(Long id);
    ApprovalTask save(ApprovalTask task);
    void update(ApprovalTask task);
    List<ApprovalTask> findByInstanceId(Long instanceId);
    List<ApprovalTask> findByApproverIdAndStatus(Long approverId, String status);
    void deleteById(Long id);
}
