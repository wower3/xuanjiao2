package com.xuanjiao.domain.usage.repository;

import com.xuanjiao.domain.usage.entity.UsageApply;
import java.util.List;

/**
 * 素材使用申请Repository接口
 */
public interface UsageApplyRepository {
    /**
     * 根据ID查询申请单（包含关联的素材列表）
     */
    UsageApply findById(Long id);

    /**
     * 根据条件查询申请单列表
     */
    List<UsageApply> findByCondition(String status, int offset, int limit);

    /**
     * 根据条件统计申请单数量
     */
    long countByCondition(String status);

    /**
     * 根据用户ID查询申请单列表
     */
    List<UsageApply> findByUserId(Long userId, int offset, int limit);

    /**
     * 根据用户ID查询草稿列表
     */
    List<UsageApply> findDraftsByUserId(Long userId, int offset, int limit);

    /**
     * 统计用户的申请单数量
     */
    long countByUserId(Long userId);

    /**
     * 统计用户的草稿数量
     */
    long countDraftsByUserId(Long userId);

    /**
     * 保存申请单
     */
    void save(UsageApply usageApply);

    /**
     * 更新申请单
     */
    void update(UsageApply usageApply);

    /**
     * 根据ID删除申请单
     */
    void deleteById(Long id);
}
