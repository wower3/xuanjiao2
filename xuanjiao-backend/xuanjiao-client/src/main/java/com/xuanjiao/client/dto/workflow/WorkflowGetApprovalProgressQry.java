package com.xuanjiao.client.dto.workflow;

import lombok.Data;

/**
 * 获取审批进度查询对象
 *
 * <p>用于根据审批实例ID获取完整的审批进度信息，包括各阶段状态和审批人处理情况。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowGetApprovalProgressQry {

    /**
     * 审批实例ID
     */
    private Long instanceId;
}
