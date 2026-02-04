package com.xuanjiao.infrastructure.approval;

import lombok.Data;

import java.util.List;

/**
 * ApprovalTask查询条件对象
 * 用于查询审批任务
 */
@Data
public class ApprovalTaskQuery {
    private Long id;
    private Long instanceId;
    private Long stageId;
    private Long approverId;
    private String status;
    private Integer isFirstApprover; // 是否是第一个审批人：0-否，1-是
    private String taskType; // 任务类型：NORMAL, RESTART_SUB_WORKFLOW

    // 扩展查询条件
    private Boolean subWorkflowApproverIdsNotNull; // IS NOT NULL查询：sub_workflow_approver_ids不为空
    private List<String> statusIn; // IN查询：状态列表
    private Long idNotEqual; // != 查询：排除指定ID的任务
}
