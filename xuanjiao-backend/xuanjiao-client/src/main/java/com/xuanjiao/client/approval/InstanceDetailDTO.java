package com.xuanjiao.client.approval;

import com.xuanjiao.client.asset.AssetDTO;
import com.xuanjiao.client.user.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批实例详情数据传输对象
 *
 * <p>用于在审批实例详情接口中返回实例的完整信息，
 * 包括实例信息、流程信息、业务数据、审批进度等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class InstanceDetailDTO {

    // ===== 实例基础信息 =====

    /**
     * 审批实例ID
     */
    private Long id;

    /**
     * 实例状态（PENDING-待审批、APPROVED-已通过、REJECTED-已驳回、MAIN_COMPLETED-主流程完成等待子流程）
     */
    private String status;

    /**
     * 业务类型（MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除）
     */
    private String businessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    // ===== 流程信息 =====

    /**
     * 流程定义ID
     */
    private Long workflowId;

    /**
     * 流程名称
     */
    private String workflowName;

    // ===== 业务信息（素材录入） =====

    /**
     * 申请单ID
     */
    private Long applicationId;

    /**
     * 申请单标题
     */
    private String applicationTitle;

    /**
     * 业务名称（兼容旧字段）
     */
    private String businessName;

    /**
     * 素材类型
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

    // ===== 素材列表 =====

    /**
     * 素材列表
     */
    private List<AssetDTO> assets;

    // ===== 申请人信息 =====

    /**
     * 申请人
     */
    private UserDTO applicant;

    // ===== 审批进度 =====

    /**
     * 审批进度列表
     */
    private List<ApprovalProgressDTO> approvalProgress;

    // ===== 当前状态 =====

    /**
     * 当前阶段ID
     */
    private Long currentStageId;

    /**
     * 当前阶段名称
     */
    private String currentStageName;

    /**
     * 是否可以选择下一阶段审批人
     */
    private Boolean canSelectNextApprovers;
}
