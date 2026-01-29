package com.xuanjiao.domain.asset.repository;

import com.xuanjiao.domain.asset.entity.Asset;
import java.util.List;

public interface AssetRepository {
    Asset findById(Long id);
    Asset findByMd5(String md5);
    List<Asset> findByCondition(String name, String type, String status, int offset, int limit);
    List<Asset> findByStatusList(String name, String type, List<String> statusList, int offset, int limit);
    List<Asset> findByApplicationId(Long applicationId);
    long countByCondition(String name, String type, String status);
    long countByStatusList(String name, String type, List<String> statusList);
    void save(Asset asset);
    void update(Asset asset);
    void deleteById(Long id);
}
