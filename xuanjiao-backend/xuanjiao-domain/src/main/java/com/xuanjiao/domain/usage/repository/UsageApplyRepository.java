package com.xuanjiao.domain.usage.repository;

import com.xuanjiao.domain.usage.entity.UsageApply;
import java.util.List;

public interface UsageApplyRepository {
    UsageApply findById(Long id);
    UsageApply findByAssetAndUser(Long assetId, Long userId, String status);
    List<UsageApply> findByCondition(Long assetId, String status, int offset, int limit);
    long countByCondition(Long assetId, String status);
    void save(UsageApply usageApply);
    void update(UsageApply usageApply);
    void deleteById(Long id);
}
