package com.xuanjiao.domain.approval.repository;

import com.xuanjiao.domain.approval.entity.ApprovalInstance;
import java.util.List;

public interface ApprovalInstanceRepository {
    ApprovalInstance findById(Long id);
    ApprovalInstance save(ApprovalInstance instance);
    void update(ApprovalInstance instance);
    List<ApprovalInstance> findByApplicantId(Long applicantId);
    List<ApprovalInstance> findByBusinessTypeAndBusinessId(String businessType, Long businessId);
}
