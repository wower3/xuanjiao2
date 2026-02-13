package com.xuanjiao.client.dto.approval.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待办任务数据传输对象
 *
 * <p>用于在前后端之间传输待办任务信息，展示当前用户需要处理的审批任务，
 * 包括任务基本信息、业务类型、申请单信息等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class PendingTaskDTO {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务状态（PENDING-待审批）
     */
    private String status;

    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;

    /**
     * 任务类型（NORMAL-普通审批、RESTART_SUB_WORKFLOW-重新发起子流程）
     */
    private String taskType;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 阶段ID
     */
    private Long stageId;

    /**
     * 子流程审批人ID列表（JSON格式）
     */
    private String subWorkflowApproverIds;

    /**
     * 审批实例ID
     */
    private Long instanceId;

    /**
     * 业务类型（MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除）
     */
    private String businessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 申请单ID（素材录入/删除申请）
     */
    private Long applicationId;

    /**
     * 申请单标题
     */
    private String applicationTitle;

    /**
     * 业务名称（用于前端显示）
     */
    private String businessName;

    /**
     * 素材类型（IMAGE-图片、VIDEO-视频、DOCUMENT-文档）
     */
    private String assetType;

    /**
     * 素材数量
     */
    private Integer assetCount;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;
}
