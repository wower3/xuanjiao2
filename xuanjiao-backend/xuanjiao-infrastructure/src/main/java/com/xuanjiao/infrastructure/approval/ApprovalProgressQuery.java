package com.xuanjiao.infrastructure.approval;

import lombok.Data;

import java.util.List;

/**
 * 审批进度查询条件对象
 *
 * <p>用于动态构建审批进度查询条件，对应 ApprovalProgressMapper 使用。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalProgressQuery {

    /**
     * 进度ID
     */
    private Long id;

    /**
     * 审批实例ID
     */
    private Long instanceId;

    /**
     * 阶段ID
     */
    private Long stageId;

    /**
     * 进度状态（PENDING, APPROVED, REJECTED, SKIPPED）
     */
    private String status;

    /**
     * 是否是子流程（0-否、1-是）
     */
    private Integer isSubWorkflow;

    /**
     * 父实例ID（用于查询子流程）
     */
    private Long parentInstanceId;

    /**
     * 父任务ID
     */
    private Long parentTaskId;

    /**
     * 父实例ID是否为空（IS NULL 查询，用于查询主流程）
     */
    private Boolean parentInstanceIdIsNull;

    /**
     * 实例ID列表（IN 查询）
     */
    private List<Long> instanceIds;
}
