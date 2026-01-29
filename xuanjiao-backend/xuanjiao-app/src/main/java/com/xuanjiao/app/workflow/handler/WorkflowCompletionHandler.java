package com.xuanjiao.app.workflow.handler;

/**
 * 审批完成回调处理器接口
 *
 * 使用策略模式，将不同业务类型的审批完成处理逻辑解耦。
 * 新增业务类型时，只需实现此接口并添加 @Component 注解即可，
 * 无需修改 WorkflowEngineService 和 ApprovalService 的核心代码。
 */
public interface WorkflowCompletionHandler {

    /**
     * 审批通过时的处理
     *
     * @param businessId 业务数据ID（如：素材ID、申请单ID）
     * @param instanceId 审批实例ID
     */
    void onApproved(Long businessId, Long instanceId);

    /**
     * 审批驳回时的处理
     *
     * @param businessId 业务数据ID（如：素材ID、申请单ID）
     * @param instanceId 审批实例ID
     * @param reason 驳回原因
     */
    void onRejected(Long businessId, Long instanceId, String reason);

    /**
     * 获取支持的业务类型
     *
     * @return 业务类型标识（如：MATERIAL_ENTRY、ASSET_USAGE）
     */
    String getSupportedBusinessType();
}
