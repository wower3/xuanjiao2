package com.xuanjiao.domain.log.repository;

import com.xuanjiao.domain.log.entity.UsageLog;
import java.util.List;

public interface UsageLogRepository {
    UsageLog save(UsageLog log);
    List<UsageLog> findByUserId(Long userId);
    List<UsageLog> findByAssetId(Long assetId);
    List<UsageLog> findAll();
}
