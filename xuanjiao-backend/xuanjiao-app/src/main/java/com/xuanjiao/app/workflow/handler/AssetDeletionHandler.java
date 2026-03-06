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
 * <p>处理素材删除申请（ASSET_DELETION）的审批通过和驳回逻辑。
 * 审批通过时将素材状态标记为DELETED，审批驳回时更新申请单状态为REJECTED。</p>
 *
 * <p>使用 ApplicationContext 延迟获取依赖 Bean，避免循环依赖问题。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see WorkflowCompletionHandler
 */
@Component
public class AssetDeletionHandler implements WorkflowCompletionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AssetDeletionHandler.class);

    /** 状态常量 */
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    /** 业务类型常量 */
    private static final String BUSINESS_TYPE_ASSET_DELETION = "ASSET_DELETION";

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 获取 AssetDeletionApplicationService Bean
     *
     * <p>使用 ApplicationContext 延迟获取，避免循环依赖。</p>
     *
     * @return 素材删除申请服务实例，获取失败返回null
     */
    private AssetDeletionApplicationService getAssetDeletionApplicationService() {
        try {
            return applicationContext.getBean(AssetDeletionApplicationService.class);
        } catch (BeansException e) {
            logger.error("获取 AssetDeletionApplicationService 失败", e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>素材删除审批通过处理：</p>
     * <ol>
     *   <li>更新申请单状态为APPROVED</li>
     *   <li>将关联素材状态标记为DELETED</li>
     *   <li>记录素材删除审批时间</li>
     * </ol>
     */
    @Override
    public void onApproved(Long businessId, Long instanceId) {
        logger.info("素材删除审批通过: businessId={}, instanceId={}", businessId, instanceId);
        try {
            AssetDeletionApplicationService deletionApplicationService = getAssetDeletionApplicationService();

            if (deletionApplicationService != null) {
                // 更新申请单状态为已通过
                deletionApplicationService.updateStatus(businessId, STATUS_APPROVED);

                // 执行素材删除逻辑（标记素材状态为DELETED）
                deletionApplicationService.approveDeletion(businessId);
            }

            logger.info("素材删除审批通过处理完成: businessId={}", businessId);
        } catch (Exception e) {
            logger.error("素材删除审批通过处理失败: businessId={}, error={}", businessId, e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>素材删除审批驳回处理：更新申请单状态为REJECTED。</p>
     */
    @Override
    public void onRejected(Long businessId, Long instanceId, String reason) {
        logger.info("素材删除审批驳回: businessId={}, instanceId={}, reason={}", businessId, instanceId, reason);
        try {
            AssetDeletionApplicationService deletionApplicationService = getAssetDeletionApplicationService();

            if (deletionApplicationService != null) {
                // 更新申请单状态为已驳回
                deletionApplicationService.updateStatus(businessId, STATUS_REJECTED);
            }

            logger.info("素材删除审批驳回处理完成: businessId={}", businessId);
        } catch (Exception e) {
            logger.error("素材删除审批驳回处理失败: businessId={}, error={}", businessId, e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return "ASSET_DELETION"
     */
    @Override
    public String getSupportedBusinessType() {
        return BUSINESS_TYPE_ASSET_DELETION;
    }
}
