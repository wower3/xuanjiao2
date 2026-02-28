package com.xuanjiao.client.workflow;

import lombok.Data;

/**
 * 获取第一层可选审批人查询对象
 *
 * <p>用于查询工作流第一阶段的可选审批人列表，支持按关键词搜索。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowGetFirstStageApproversQry {

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 搜索关键词（匹配用户名或姓名）
     */
    private String keyword;
}
