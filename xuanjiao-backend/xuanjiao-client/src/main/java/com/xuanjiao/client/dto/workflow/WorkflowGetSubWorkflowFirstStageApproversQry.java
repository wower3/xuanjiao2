package com.xuanjiao.client.dto.workflow;

import lombok.Data;

/**
 * 获取子流程第一层可选审批人查询对象
 */
@Data
public class WorkflowGetSubWorkflowFirstStageApproversQry {

    private Long subWorkflowId;

    private Long applicantId;

    private String keyword;
}
