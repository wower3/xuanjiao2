package com.xuanjiao.domain.material.repository;

import com.xuanjiao.domain.material.entity.MaterialApplication;

import java.util.List;

/**
 * 素材录入申请仓储接口
 *
 * <p>定义素材录入申请的持久化操作。</p>
 * <p>素材录入申请需要经过审批流程才能将素材正式纳入系统。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface MaterialApplicationRepository {

    /**
     * 根据ID查找素材录入申请
     *
     * @param id 申请ID
     * @return 素材录入申请实体，如果不存在返回 null
     */
    MaterialApplication findById(Long id);

    /**
     * 保存素材录入申请
     *
     * @param application 素材录入申请实体
     * @return 保存后的素材录入申请
     */
    MaterialApplication save(MaterialApplication application);

    /**
     * 更新素材录入申请
     *
     * @param application 素材录入申请实体
     * @return 更新后的素材录入申请
     */
    MaterialApplication update(MaterialApplication application);

    /**
     * 根据ID删除素材录入申请
     *
     * @param id 申请ID
     */
    void deleteById(Long id);

    /**
     * 根据申请人和状态查找素材录入申请列表
     *
     * @param applicantId 申请人ID
     * @param status 申请状态
     * @return 匹配的素材录入申请列表
     */
    List<MaterialApplication> findByApplicantAndStatus(Long applicantId, String status);

    /**
     * 根据申请人分页查找素材录入申请列表
     *
     * @param applicantId 申请人ID
     * @param offset 分页偏移量
     * @param limit 分页大小
     * @return 该申请人的素材录入申请列表
     */
    List<MaterialApplication> findByApplicant(Long applicantId, int offset, int limit);

    /**
     * 统计申请人的素材录入申请数量
     *
     * @param applicantId 申请人ID
     * @return 申请数量
     */
    long countByApplicant(Long applicantId);
}
