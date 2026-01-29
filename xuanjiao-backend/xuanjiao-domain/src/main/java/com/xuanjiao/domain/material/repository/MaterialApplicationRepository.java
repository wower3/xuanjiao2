package com.xuanjiao.domain.material.repository;

import com.xuanjiao.domain.material.entity.MaterialApplication;

import java.util.List;

public interface MaterialApplicationRepository {
    MaterialApplication findById(Long id);
    MaterialApplication save(MaterialApplication application);
    MaterialApplication update(MaterialApplication application);
    void deleteById(Long id);
    List<MaterialApplication> findByApplicantAndStatus(Long applicantId, String status);
    List<MaterialApplication> findByApplicant(Long applicantId, int offset, int limit);
    long countByApplicant(Long applicantId);
}
