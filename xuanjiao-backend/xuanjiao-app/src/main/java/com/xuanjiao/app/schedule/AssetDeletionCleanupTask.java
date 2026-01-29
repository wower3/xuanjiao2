package com.xuanjiao.app.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 素材彻底软删除定时任务
 * 每天凌晨2点执行，清理审批通过超过一周的素材
 */
@Component
public class AssetDeletionCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(AssetDeletionCleanupTask.class);

    @Resource
    private AssetMapper assetMapper;

    /**
     * 每天凌晨2点执行
     * 将状态为DELETED且删除审批时间超过一周的素材彻底软删除
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupDeletedAssets() {
        cleanupDeletedAssetsInternal();
    }

    /**
     * 手动触发方法（用于测试）
     * 可以通过后端接口调用此方法进行测试
     * @return 删除的素材数量
     */
    public int cleanupDeletedAssetsManually() {
        logger.info("手动触发素材彻底软删除任务");
        return cleanupDeletedAssetsInternal();
    }

    /**
     * 内部执行方法，返回删除数量
     */
    private int cleanupDeletedAssetsInternal() {
        logger.info("开始执行素材彻底软删除定时任务");

        try {
            // 计算一周前的时间
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

            // 使用 LambdaUpdateWrapper 批量更新deleted字段为1
            LambdaUpdateWrapper<AssetDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(AssetDO::getStatus, "DELETED")
                   .lt(AssetDO::getDeletionApproveTime, oneWeekAgo)
                   .eq(AssetDO::getDeleted, 0)  // 未被彻底软删除的
                   .set(AssetDO::getDeleted, 1)
                   .set(AssetDO::getUpdateTime, LocalDateTime.now());

            int updatedCount = assetMapper.update(null, wrapper);

            logger.info("素材彻底软删除定时任务执行完成，共处理 {} 条记录", updatedCount);
            return updatedCount;
        } catch (Exception e) {
            logger.error("素材彻底软删除定时任务执行失败", e);
            return 0;
        }
    }
}
