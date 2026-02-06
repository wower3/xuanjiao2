package com.xuanjiao.infrastructure.approval;

import lombok.Data;

import java.util.List;

/**
 * ApprovalProgress查询条件对象
 * 用于查询审批进度
 */
@Data
public class ApprovalProgressQuery {
    private Long id;
    private Long instanceId;
    private Long stageId;
    private String status;
    private Integer isSubWorkflow; // 是否是子流程：0-否，1-是
    private Long parentInstanceId; // 父实例ID（用于查询子流程）
    private Long parentTaskId; // 父任务ID

    // 扩展查询条件
    private Boolean parentInstanceIdIsNull; // IS NULL查询：父实例ID为空（用于查询主流程）
    private List<Long> instanceIds; // IN查询：实例ID列表
}
