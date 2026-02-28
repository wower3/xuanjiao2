package com.xuanjiao.domain.log.repository;

import com.xuanjiao.domain.log.entity.UsageLog;
import java.util.List;

/**
 * 素材使用日志仓储接口
 *
 * <p>定义素材使用日志的持久化操作。</p>
 * <p>使用日志用于记录用户对素材的使用行为，包括下载、查看等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface UsageLogRepository {

    /**
     * 保存使用日志
     *
     * @param log 使用日志实体
     * @return 保存后的使用日志
     */
    UsageLog save(UsageLog log);

    /**
     * 根据用户ID查找使用日志列表
     *
     * @param userId 用户ID
     * @return 该用户的使用日志列表
     */
    List<UsageLog> findByUserId(Long userId);

    /**
     * 根据素材ID查找使用日志列表
     *
     * @param assetId 素材ID
     * @return 该素材的使用日志列表
     */
    List<UsageLog> findByAssetId(Long assetId);

    /**
     * 查找所有使用日志
     *
     * @return 所有使用日志列表
     */
    List<UsageLog> findAll();
}
