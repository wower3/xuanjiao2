package com.xuanjiao.app.workflow.handler;

import com.xuanjiao.app.material.MaterialApplicationService;
import com.xuanjiao.app.asset.AssetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 素材录入审批完成处理器
 *
 * 处理素材录入申请（MATERIAL_ENTRY）的审批通过和驳回逻辑
 */
@Component
public class MaterialEntryHandler implements WorkflowCompletionHandler {

    private static final Logger logger = LoggerFactory.getLogger(MaterialEntryHandler.class);

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 获取 MaterialApplicationService Bean
     * 使用 ApplicationContext 延迟获取，避免循环依赖
     */
    private MaterialApplicationService getMaterialApplicationService() {
        try {
            return applicationContext.getBean(MaterialApplicationService.class);
        } catch (BeansException e) {
            logger.error("获取 MaterialApplicationService 失败", e);
            return null;
        }
    }

    /**
     * 获取 AssetService Bean
     * 使用 ApplicationContext 延迟获取，避免循环依赖
     */
    private AssetService getAssetService() {
        try {
            return applicationContext.getBean(AssetService.class);
        } catch (BeansException e) {
            logger.error("获取 AssetService 失败", e);
            return null;
        }
    }

    @Override
    public void onApproved(Long businessId, Long instanceId) {
        logger.info("素材录入审批通过: businessId={}, instanceId={}", businessId, instanceId);
        try {
            MaterialApplicationService materialApplicationService = getMaterialApplicationService();
            AssetService assetService = getAssetService();

            if (materialApplicationService != null) {
                materialApplicationService.updateStatus(businessId, "APPROVED");
            }
            if (assetService != null) {
                assetService.updateStatusByApplicationId(businessId, "APPROVED");
            }
            logger.info("素材录入审批通过处理完成: businessId={}", businessId);
        } catch (Exception e) {
            logger.error("素材录入审批通过处理失败: businessId={}, error={}", businessId, e.getMessage(), e);
        }
    }

    @Override
    public void onRejected(Long businessId, Long instanceId, String reason) {
        logger.info("素材录入审批驳回: businessId={}, instanceId={}, reason={}", businessId, instanceId, reason);
        try {
            MaterialApplicationService materialApplicationService = getMaterialApplicationService();
            AssetService assetService = getAssetService();

            if (materialApplicationService != null) {
                materialApplicationService.updateStatus(businessId, "REJECTED");
            }
            if (assetService != null) {
                assetService.updateStatusByApplicationId(businessId, "REJECTED");
            }
            logger.info("素材录入审批驳回处理完成: businessId={}", businessId);
        } catch (Exception e) {
            logger.error("素材录入审批驳回处理失败: businessId={}, error={}", businessId, e.getMessage(), e);
        }
    }

    @Override
    public String getSupportedBusinessType() {
        return "MATERIAL_ENTRY";
    }
}
