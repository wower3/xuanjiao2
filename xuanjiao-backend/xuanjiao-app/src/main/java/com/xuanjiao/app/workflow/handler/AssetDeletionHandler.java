package com.xuanjiao.app.workflow.handler;

import com.xuanjiao.app.deletion.AssetDeletionApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 素材删除审批完成处理器
 *
 * 处理素材删除申请（ASSET_DELETION）的审批通过和驳回逻辑
 */
@Component
public class AssetDeletionHandler implements WorkflowCompletionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AssetDeletionHandler.class);

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 获取 AssetDeletionApplicationService Bean
     * 使用 ApplicationContext 延迟获取，避免循环依赖
     */
    private AssetDeletionApplicationService getAssetDeletionApplicationService() {
        try {
            return applicationContext.getBean(AssetDeletionApplicationService.class);
        } catch (BeansException e) {
            logger.error("获取 AssetDeletionApplicationService 失败", e);
            return null;
        }
    }

    @Override
    public void onApproved(Long businessId, Long instanceId) {
        logger.info("素材删除审批通过: businessId={}, instanceId={}", businessId, instanceId);
        try {
            AssetDeletionApplicationService deletionApplicationService = getAssetDeletionApplicationService();

            if (deletionApplicationService != null) {
                // 更新申请单状态为已通过
                deletionApplicationService.updateStatus(businessId, "APPROVED");

                // 执行素材删除逻辑（标记素材状态为DELETED）
                deletionApplicationService.approveDeletion(businessId);
            }

            logger.info("素材删除审批通过处理完成: businessId={}", businessId);
        } catch (Exception e) {
            logger.error("素材删除审批通过处理失败: businessId={}, error={}", businessId, e.getMessage(), e);
        }
    }

    @Override
    public void onRejected(Long businessId, Long instanceId, String reason) {
        logger.info("素材删除审批驳回: businessId={}, instanceId={}, reason={}", businessId, instanceId, reason);
        try {
            AssetDeletionApplicationService deletionApplicationService = getAssetDeletionApplicationService();

            if (deletionApplicationService != null) {
                // 更新申请单状态为已驳回
                deletionApplicationService.updateStatus(businessId, "REJECTED");
            }

            logger.info("素材删除审批驳回处理完成: businessId={}", businessId);
        } catch (Exception e) {
            logger.error("素材删除审批驳回处理失败: businessId={}, error={}", businessId, e.getMessage(), e);
        }
    }

    @Override
    public String getSupportedBusinessType() {
        return "ASSET_DELETION";
    }
}
