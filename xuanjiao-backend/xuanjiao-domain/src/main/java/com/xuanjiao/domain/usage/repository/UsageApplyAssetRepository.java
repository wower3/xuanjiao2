package com.xuanjiao.domain.usage.repository;

import com.xuanjiao.domain.usage.entity.UsageApplyAsset;
import java.util.List;

/**
 * 素材使用申请-素材关联Repository接口
 */
public interface UsageApplyAssetRepository {
    /**
     * 根据申请单ID查询关联的素材列表
     */
    List<UsageApplyAsset> findByUsageApplyId(Long usageApplyId);

    /**
     * 根据素材ID查询关联的申请单列表
     */
    List<UsageApplyAsset> findByAssetId(Long assetId);

    /**
     * 根据申请单ID和素材ID查询
     */
    UsageApplyAsset findByUsageApplyIdAndAssetId(Long usageApplyId, Long assetId);

    /**
     * 批量保存申请单-素材关联
     */
    void batchSave(List<UsageApplyAsset> usageApplyAssets);

    /**
     * 根据申请单ID删除所有关联
     */
    void deleteByUsageApplyId(Long usageApplyId);

    /**
     * 删除单个关联
     */
    void deleteById(Long id);

    /**
     * 检查素材是否被指定的申请单使用
     */
    boolean existsByAssetIdAndUsageApplyId(Long assetId, Long usageApplyId);
}
