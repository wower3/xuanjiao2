package com.xuanjiao.client.dto.approval.dto;

import lombok.Data;
import java.util.List;

/**
 * 审批实例详情数据传输对象
 *
 * <p>用于在前后端之间传输审批实例的完整信息，包括：
 * <ul>
 *   <li>实例基本信息（ID、状态、业务类型、申请单标题等）</li>
 *   <li>申请人信息（ID、姓名、部门）</li>
 *   <li>当前阶段信息（ID、名称、审批类型）</li>
 *   <li>待审批人列表（当前阶段所有审批人）</li>
 *   <li>审批进度（每个阶段的审批情况）</li>
 *   <li>素材列表（素材录入、使用、删除）</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalInstanceDetailDTO {

    /**
     * 实例ID
     */
    private Long id;

    /**
     * 实例ID（前端兼容字段，与 id 相同）
     */
    private Long instanceId;

    /**
     * 审批状态（PENDING-待审批、APPROVED-已通过、REJECTED-已驳回、MAIN_COMPLETED-主流程完成）
     */
    private String status;

    /**
     * 业务类型（MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除）
     */
    private String businessType;

    /**
     * 业务ID（申请单ID或素材ID）
     */
    private Long businessId;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 申请单ID
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
     * 素材状态
     */
    private String assetStatus;

    /**
     * 素材数量
     */
    private Integer assetCount;

    /**
     * 素材列表
     */
    private List<ApprovalAssetInfoDTO> assetList;

    /**
     * 素材列表（前端期望的字段名）
     */
    private List<ApprovalAssetInfoDTO> assets;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 当前阶段ID
     */
    private Long currentStageId;

    /**
     * 当前阶段名称
     */
    private String currentStageName;

    /**
     * 审批类型（AND-会签，需要所有审批人通过；OR-或签，任一审批人通过即可）
     */
    private String approveType;

    /**
     * 待审批人列表（当前阶段所有审批人）
     */
    private List<ApproverSimpleInfo> pendingApprovers;

    /**
     * 审批进度列表（每个阶段的审批进度）
     */
    private List<ApprovalProgressDTO> approvalProgress;

    /**
     * 删除原因（仅 ASSET_DELETION 类型）
     */
    private String deleteReason;

    /**
     * 审批人简单信息
     */
    @Data
    public static class ApproverSimpleInfo {
        private Long id;
        private String name;
    }
}
