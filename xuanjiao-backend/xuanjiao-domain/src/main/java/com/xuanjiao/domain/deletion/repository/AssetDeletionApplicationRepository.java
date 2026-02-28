package com.xuanjiao.domain.deletion.repository;

import com.xuanjiao.domain.deletion.entity.AssetDeletionApplication;

import java.util.List;

/**
 * 素材删除申请仓储接口
 *
 * <p>定义素材删除申请的持久化操作。</p>
 * <p>删除申请需要经过审批流程，审批通过后素材进入删除待清理状态。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface AssetDeletionApplicationRepository {

    /**
     * 根据ID查找删除申请
     *
     * @param id 申请ID
     * @return 删除申请实体，如果不存在返回 null
     */
    AssetDeletionApplication findById(Long id);

    /**
     * 保存删除申请
     *
     * @param application 删除申请实体
     * @return 保存后的删除申请
     */
    AssetDeletionApplication save(AssetDeletionApplication application);

    /**
     * 更新删除申请
     *
     * @param application 删除申请实体
     * @return 更新后的删除申请
     */
    AssetDeletionApplication update(AssetDeletionApplication application);

    /**
     * 根据ID删除申请
     *
     * @param id 申请ID
     */
    void deleteById(Long id);

    /**
     * 根据申请人和状态查找删除申请列表
     *
     * @param applicantId 申请人ID
     * @param status 申请状态
     * @return 匹配的删除申请列表
     */
    List<AssetDeletionApplication> findByApplicantAndStatus(Long applicantId, String status);

    /**
     * 根据申请人分页查找删除申请列表
     *
     * @param applicantId 申请人ID
     * @param offset 分页偏移量
     * @param limit 分页大小
     * @return 该申请人的删除申请列表
     */
    List<AssetDeletionApplication> findByApplicant(Long applicantId, int offset, int limit);

    /**
     * 统计申请人的删除申请数量
     *
     * @param applicantId 申请人ID
     * @return 申请数量
     */
    long countByApplicant(Long applicantId);
}
