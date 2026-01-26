package com.xuanjiao.app.workflow.handler;

import com.xuanjiao.app.usage.UsageApplyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 素材使用审批完成处理器
 *
 * 处理素材使用申请（ASSET_USAGE）的审批通过和驳回逻辑
 */
@Component
public class AssetUsageHandler implements WorkflowCompletionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AssetUsageHandler.class);

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 获取 UsageApplyService Bean
     * 使用 ApplicationContext 延迟获取，避免循环依赖
     */
    private UsageApplyService getUsageApplyService() {
        try {
            return applicationContext.getBean(UsageApplyService.class);
        } catch (BeansException e) {
            logger.error("获取 UsageApplyService 失败", e);
            return null;
        }
    }

    @Override
    public void onApproved(Long businessId, Long instanceId) {
        logger.info("素材使用审批通过: businessId={}, instanceId={}", businessId, instanceId);
        try {
            UsageApplyService usageApplyService = getUsageApplyService();
            if (usageApplyService != null) {
                usageApplyService.updateStatus(businessId, "APPROVED");
            }
            logger.info("素材使用审批通过处理完成: businessId={}", businessId);
        } catch (Exception e) {
            logger.error("素材使用审批通过处理失败: businessId={}, error={}", businessId, e.getMessage(), e);
            // 不抛出异常，避免影响审批流程
        }
    }

    @Override
    public void onRejected(Long businessId, Long instanceId, String reason) {
        logger.info("素材使用审批驳回: businessId={}, instanceId={}, reason={}", businessId, instanceId, reason);
        try {
            UsageApplyService usageApplyService = getUsageApplyService();
            if (usageApplyService != null) {
                usageApplyService.updateStatus(businessId, "REJECTED");
            }
            logger.info("素材使用审批驳回处理完成: businessId={}", businessId);
        } catch (Exception e) {
            logger.error("素材使用审批驳回处理失败: businessId={}, error={}", businessId, e.getMessage(), e);
            // 不抛出异常，避免影响审批流程
        }
    }

    @Override
    public String getSupportedBusinessType() {
        return "ASSET_USAGE";
    }
}
