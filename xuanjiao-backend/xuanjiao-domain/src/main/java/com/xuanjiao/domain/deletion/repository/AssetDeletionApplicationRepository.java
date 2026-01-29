package com.xuanjiao.domain.deletion.repository;

import com.xuanjiao.domain.deletion.entity.AssetDeletionApplication;

import java.util.List;

/**
 * 素材删除申请Repository接口
 */
public interface AssetDeletionApplicationRepository {
    AssetDeletionApplication findById(Long id);
    AssetDeletionApplication save(AssetDeletionApplication application);
    AssetDeletionApplication update(AssetDeletionApplication application);
    void deleteById(Long id);
    List<AssetDeletionApplication> findByApplicantAndStatus(Long applicantId, String status);
    List<AssetDeletionApplication> findByApplicant(Long applicantId, int offset, int limit);
    long countByApplicant(Long applicantId);
}
