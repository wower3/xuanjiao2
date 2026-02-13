package com.xuanjiao.client.dto.approval.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 审批任务详情数据传输对象
 *
 * <p>用于在前后端之间传输审批任务的详细信息，包括业务数据、
 * 当前阶段、下一阶段配置、审批进度等信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalTaskDetailDTO {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 任务类型（NORMAL-普通审批、RESTART_SUB_WORKFLOW-重新发起子流程）
     */
    private String taskType;

    /**
     * 是否为当前阶段的第一审批人
     */
    private Integer isFirstApprover;

    /**
     * 下一阶段审批人ID列表（JSON格式）
     */
    private String nextStageApproverIds;

    /**
     * 选中该审批人的用户ID
     */
    private Long selectedByUserId;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 子流程审批人ID列表（JSON格式）
     */
    private String subWorkflowApproverIds;

    /**
     * 实例ID
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
     * 申请单标题
     */
    private String applicationTitle;

    /**
     * 业务名称
     */
    private String businessName;

    /**
     * 申请ID
     */
    private Long applicationId;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 阶段ID
     */
    private Long stageId;

    /**
     * 阶段名称
     */
    private String stageName;

    /**
     * 当前阶段ID
     */
    private Long currentStageId;

    /**
     * 当前阶段名称
     */
    private String currentStageName;

    /**
     * 当前阶段审批类型（AND-会签、OR-或签）
     */
    private String approveType;

    /**
     * 素材类型（IMAGE-图片、VIDEO-视频、DOCUMENT-文档）
     */
    private String assetType;

    /**
     * 素材数量
     */
    private Integer assetCount;

    /**
     * 删除原因
     */
    private String deleteReason;

    /**
     * 下一阶段ID
     */
    private Long nextStageId;

    /**
     * 下一阶段名称
     */
    private String nextStageName;

    /**
     * 下一阶段审批类型
     */
    private String nextStageApproveType;

    /**
     * 下一阶段审批人配置列表
     */
    private List<ApproverConfigDTO> nextStageApprovers;

    /**
     * 下一阶段审批人配置
     */
    private List<ApproverConfigDTO> nextStageApproverConfigs;

    /**
     * 下一阶段审批人数量
     */
    private Integer nextStageApproverCount;

    /**
     * 当前阶段的其他审批人（不包含自己）
     */
    private List<OtherApproverInfo> otherApprovers;

    /**
     * 子流程配置列表
     */
    private List<SubWorkflowConfigDTO> subWorkflowConfigs;

    /**
     * 子流程配置
     */
    private List<SubWorkflowConfigDTO> subWorkflows;

    /**
     * 是否有子流程
     */
    private Boolean hasSubWorkflows;

    /**
     * 审批进度列表
     */
    private List<ApprovalProgressDTO> approvalProgress;

    /**
     * 是否为最后阶段
     */
    private Boolean isLastStage;

    /**
     * 审批人数量
     */
    private Integer approverCount;

    /**
     * 是否可以选择下一阶段审批人
     */
    private Boolean canSelectNextApprovers;

    /**
     * 已选择的下一阶段审批人
     */
    private List<ApproverSelectionDTO> selectedNextApprovers;

    /**
     * 已选择的子流程审批人
     */
    private Map<Long, List<ApproverSelectionDTO>> selectedSubWorkflowApprovers;

    /**
     * 审批人简单信息
     */
    @Data
    public static class ApproverSimpleInfo {
        private Long id;
        private String name;
    }

    /**
     * 其他审批人信息
     */
    @Data
    public static class OtherApproverInfo {
        private Long id;
        private String name;
        private String status;
    }
}
