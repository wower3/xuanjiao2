package com.xuanjiao.client.dto.workflow;

import lombok.Data;

/**
 * 获取第一层可选审批人查询对象
 */
@Data
public class WorkflowGetFirstStageApproversQry {

    private Long workflowId;

    private Long applicantId;

    private String keyword;
}
