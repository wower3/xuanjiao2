package com.xuanjiao.app.schedule;

import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.asset.AssetQuery;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 素材彻底软删除定时任务
 *
 * <p>每天凌晨2点执行，清理审批通过超过一周的素材。
 * 实现素材删除的两阶段处理：</p>
 *
 * <ol>
 *   <li>第一阶段：审批通过后素材状态变为DELETED（可见但不可用）</li>
 *   <li>第二阶段：7天后执行软删除，设置deleted=1（完全隐藏）</li>
 * </ol>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Component
public class AssetDeletionCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(AssetDeletionCleanupTask.class);

    @Resource
    private AssetMapper assetMapper;

    /**
     * 每天凌晨2点执行定时任务
     *
     * <p>将状态为DELETED且删除审批时间超过一周的素材彻底软删除。
     * 执行条件：</p>
     * <ul>
     *   <li>素材状态为DELETED</li>
     *   <li>deletion_approve_time 早于一周前</li>
     *   <li>deleted = 0（未执行过软删除）</li>
     * </ul>
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupDeletedAssets() {
        cleanupDeletedAssetsInternal();
    }

    /**
     * 手动触发方法（用于测试和管理）
     *
     * <p>可以通过后端管理员接口调用此方法进行测试或立即执行清理。</p>
     *
     * @return 删除的素材数量
     */
    public int cleanupDeletedAssetsManually() {
        logger.info("手动触发素材彻底软删除任务");
        return cleanupDeletedAssetsInternal();
    }

    /**
     * 内部执行方法
     *
     * <p>执行素材软删除的核心逻辑，返回处理的记录数量。</p>
     *
     * @return 删除的素材数量，执行失败返回0
     */
    private int cleanupDeletedAssetsInternal() {
        logger.info("开始执行素材彻底软删除定时任务");

        try {
            // 计算一周前的时间
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            logger.info("查询条件：状态=DELETED, 删除审批时间早于{}", oneWeekAgo);

            // 先查询符合条件的记录（用于日志）
            AssetQuery query = new AssetQuery();
            query.setStatus("DELETED");
            query.setDeletionApproveTimeBefore(oneWeekAgo);
            query.setDeleted(0);

            List<AssetDO> toDelete = assetMapper.selectList(query);
            logger.info("找到符合条件的素材：{} 条", toDelete.size());
            for (AssetDO asset : toDelete) {
                logger.info("待软删除素材：id={}, name={}, status={}, deleted={}",
                    asset.getId(), asset.getName(), asset.getStatus(), asset.getDeleted());
            }

            // 使用 AssetMapper 的批量更新方法
            int updatedCount = assetMapper.cleanupDeletedAssets(oneWeekAgo);

            logger.info("素材彻底软删除定时任务执行完成，共处理 {} 条记录", updatedCount);

            // 验证更新结果（查询所有符合条件的记录，包括deleted=1）
            AssetQuery verifyQuery = new AssetQuery();
            verifyQuery.setStatus("DELETED");
            verifyQuery.setDeletionApproveTimeBefore(oneWeekAgo);
            verifyQuery.setDeleted(null); // 不过滤deleted，查询所有
            long remainingCount = assetMapper.selectCount(verifyQuery);
            logger.info("更新后剩余符合条件的记录（包括deleted=1）：{} 条", remainingCount);

            return updatedCount;
        } catch (Exception e) {
            logger.error("素材彻底软删除定时任务执行失败", e);
            return 0;
        }
    }
}
