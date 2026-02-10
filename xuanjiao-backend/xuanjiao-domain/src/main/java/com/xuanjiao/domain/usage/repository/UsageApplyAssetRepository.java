package com.xuanjiao.domain.usage.repository;

import com.xuanjiao.domain.usage.entity.UsageApplyAsset;
import java.util.List;

/**
 * 素材使用申请-素材关联仓储接口
 *
 * <p>定义使用申请与素材关联的持久化操作。</p>
 * <p>支持一个使用申请包含多个素材。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface UsageApplyAssetRepository {

    /**
     * 根据申请单ID查询关联的素材列表
     *
     * @param usageApplyId 使用申请ID
     * @return 该申请关联的素材列表
     */
    List<UsageApplyAsset> findByUsageApplyId(Long usageApplyId);

    /**
     * 根据素材ID查询关联的申请单列表
     *
     * @param assetId 素材ID
     * @return 该素材关联的申请列表
     */
    List<UsageApplyAsset> findByAssetId(Long assetId);

    /**
     * 根据申请单ID和素材ID查询关联记录
     *
     * @param usageApplyId 使用申请ID
     * @param assetId 素材ID
     * @return 关联记录，如果不存在返回 null
     */
    UsageApplyAsset findByUsageApplyIdAndAssetId(Long usageApplyId, Long assetId);

    /**
     * 批量保存申请单-素材关联
     *
     * @param usageApplyAssets 关联记录列表
     */
    void batchSave(List<UsageApplyAsset> usageApplyAssets);

    /**
     * 根据申请单ID删除所有关联
     *
     * @param usageApplyId 使用申请ID
     */
    void deleteByUsageApplyId(Long usageApplyId);

    /**
     * 删除单个关联
     *
     * @param id 关联记录ID
     */
    void deleteById(Long id);

    /**
     * 检查素材是否被指定的申请单使用
     *
     * @param assetId 素材ID
     * @param usageApplyId 使用申请ID
     * @return 如果存在关联返回 true，否则返回 false
     */
    boolean existsByAssetIdAndUsageApplyId(Long assetId, Long usageApplyId);
}
