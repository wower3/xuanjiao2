package com.xuanjiao.domain.usage.repository;

import com.xuanjiao.domain.usage.entity.UsageApply;
import java.util.List;

/**
 * 素材使用申请仓储接口
 *
 * <p>定义素材使用申请的持久化操作。</p>
 * <p>使用申请需要经过审批流程，审批通过后用户可以下载和使用对应素材。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface UsageApplyRepository {

    /**
     * 根据ID查询申请单
     *
     * <p>包含关联的素材列表。</p>
     *
     * @param id 申请单ID
     * @return 使用申请实体，如果不存在返回 null
     */
    UsageApply findById(Long id);

    /**
     * 根据用户ID查询申请单列表
     *
     * @param userId 用户ID
     * @param offset 分页偏移量
     * @param limit 分页大小
     * @return 该用户的使用申请列表
     */
    List<UsageApply> findByUserId(Long userId, int offset, int limit);

    /**
     * 根据用户ID查询草稿列表
     *
     * @param userId 用户ID
     * @param offset 分页偏移量
     * @param limit 分页大小
     * @return 该用户的草稿列表
     */
    List<UsageApply> findDraftsByUserId(Long userId, int offset, int limit);

    /**
     * 统计用户的申请单数量
     *
     * @param userId 用户ID
     * @return 申请单数量
     */
    long countByUserId(Long userId);

    /**
     * 统计用户的草稿数量
     *
     * @param userId 用户ID
     * @return 草稿数量
     */
    long countDraftsByUserId(Long userId);

    /**
     * 保存申请单
     *
     * @param usageApply 使用申请实体
     */
    void save(UsageApply usageApply);

    /**
     * 更新申请单
     *
     * @param usageApply 使用申请实体
     */
    void update(UsageApply usageApply);

    /**
     * 根据ID删除申请单
     *
     * @param id 申请单ID
     */
    void deleteById(Long id);
}
