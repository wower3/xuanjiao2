package com.xuanjiao.client.approval;

import com.xuanjiao.client.asset.AssetDTO;
import com.xuanjiao.client.user.UserDTO;
import com.xuanjiao.client.workflow.StageApproverDTO;
import com.xuanjiao.client.workflow.SubWorkflowDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 任务详情数据传输对象
 *
 * <p>用于在审批任务详情接口中返回任务的完整信息，
 * 包括任务信息、实例信息、流程信息、审批人配置等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class TaskDetailDTO {

    // ===== 任务基础信息 =====

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
     * 是否是第一审批人（0-否、1-是）
     */
    private Integer isFirstApprover;

    /**
     * 已选择的下一阶段审批人ID列表（JSON字符串）
     */
    private String nextStageApproverIds;

    /**
     * 选择该审批人的用户ID
     */
    private Long selectedByUserId;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 子流程审批人ID映射（JSON字符串，格式：{子流程ID: [审批人ID列表]}）
     */
    private String subWorkflowApproverIds;

    // ===== 实例信息 =====

    /**
     * 审批实例ID
     */
    private Long instanceId;

    /**
     * 业务类型（MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除）
     */
    private String businessType;

    /**
     * 业务ID（素材ID/使用申请ID/删除申请ID）
     */
    private Long businessId;

    /**
     * 流程定义ID
     */
    private Long workflowId;

    /**
     * 当前阶段ID
     */
    private Long currentStageId;

    // ===== 流程信息 =====

    /**
     * 流程名称
     */
    private String workflowName;

    // ===== 当前阶段信息 =====

    /**
     * 当前阶段ID
     */
    private Long stageId;

    /**
     * 当前阶段名称
     */
    private String stageName;

    /**
     * 当前阶段审批类型（OR-或签、AND-会签）
     */
    private String approveType;

    // ===== 下一阶段信息 =====

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

    // ===== 下一阶段审批人配置 =====

    /**
     * 下一阶段审批人配置列表
     */
    private List<StageApproverDTO> nextStageApproverConfigs;

    /**
     * 下一阶段审批人数量
     */
    private Integer nextStageApproverCount;

    // ===== 子流程信息 =====

    /**
     * 是否有子流程
     */
    private Boolean hasSubWorkflows;

    /**
     * 子流程列表
     */
    private List<SubWorkflowDTO> subWorkflows;

    // ===== 已选择的审批人 =====

    /**
     * 已选择的下一阶段审批人列表
     */
    private List<UserDTO> selectedNextApprovers;

    /**
     * 已选择的子流程审批人（子流程ID -> 审批人列表）
     */
    private Map<Long, List<UserDTO>> selectedSubWorkflowApprovers;

    // ===== 其他审批人 =====

    /**
     * 同阶段其他审批人列表
     */
    private List<UserDTO> otherApprovers;

    // ===== 业务数据 =====

    /**
     * 业务名称（素材使用申请时使用）
     */
    private String businessName;

    /**
     * 申请单ID（素材删除申请时使用）
     */
    private Long applicationId;

    /**
     * 申请单标题
     */
    private String applicationTitle;

    /**
     * 删除原因（素材删除申请时使用）
     */
    private String deleteReason;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;

    /**
     * 审批进度列表
     */
    private List<ApprovalProgressDTO> approvalProgress;

    /**
     * 素材列表（素材录入/使用/删除申请）
     */
    private List<AssetDTO> assets;

    // ===== 控制字段 =====

    /**
     * 是否有下一阶段
     */
    private Boolean hasNextStage;

    /**
     * 是否可以选择下一阶段审批人
     */
    private Boolean canSelectNextApprovers;
}
