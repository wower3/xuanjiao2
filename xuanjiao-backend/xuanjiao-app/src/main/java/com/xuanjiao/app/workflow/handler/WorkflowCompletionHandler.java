package com.xuanjiao.app.workflow.handler;

/**
 * 审批完成回调处理器接口
 *
 * <p>使用策略模式，将不同业务类型的审批完成处理逻辑解耦。
 * 新增业务类型时，只需实现此接口并添加 @Component 注解即可，
 * 无需修改 WorkflowEngineService 和 ApprovalService 的核心代码。</p>
 *
 * <p>支持的業務类型：</p>
 * <ul>
 *   <li>MATERIAL_ENTRY - 素材录入审批</li>
 *   <li>ASSET_USAGE - 素材使用审批</li>
 *   <li>ASSET_DELETION - 素材删除审批</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface WorkflowCompletionHandler {

    /**
     * 审批通过时的处理
     *
     * <p>当审批流程最终通过时调用此方法，执行业务相关的通过后处理逻辑。</p>
     *
     * @param businessId 业务数据ID（如：素材申请单ID、使用申请单ID、删除申请单ID）
     * @param instanceId 审批实例ID
     */
    void onApproved(Long businessId, Long instanceId);

    /**
     * 审批驳回时的处理
     *
     * <p>当审批流程被驳回时调用此方法，执行业务相关的驳回后处理逻辑。</p>
     *
     * @param businessId 业务数据ID（如：素材申请单ID、使用申请单ID、删除申请单ID）
     * @param instanceId 审批实例ID
     * @param reason 驳回原因，可为null
     */
    void onRejected(Long businessId, Long instanceId, String reason);

    /**
     * 获取支持的业务类型
     *
     * <p>返回此处理器支持的业务类型标识，用于策略模式匹配。</p>
     *
     * @return 业务类型标识（如：MATERIAL_ENTRY、ASSET_USAGE、ASSET_DELETION）
     */
    String getSupportedBusinessType();
}
