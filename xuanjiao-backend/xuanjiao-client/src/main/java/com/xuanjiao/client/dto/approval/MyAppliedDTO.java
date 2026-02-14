package com.xuanjiao.client.dto.approval;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 我发起的工单数据传输对象
 *
 * <p>用于在前后端之间传输当前用户发起的审批工单信息，
 * 包括工单状态、业务类型、当前阶段等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MyAppliedDTO {

    /**
     * 审批实例ID
     */
    private Long id;

    /**
     * 审批状态（PENDING-待审批、APPROVED-已通过、REJECTED-已驳回）
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
     * 申请单ID（与businessId相同，用于前端兼容）
     */
    private Long applicationId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

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
     * 当前阶段ID
     */
    private Long currentStageId;

    /**
     * 当前阶段名称
     */
    private String currentStageName;

    /**
     * 业务名称（申请单标题，用于前端显示）
     */
    private String businessName;

    /**
     * 待审批人列表
     *
     * <p>每个审批人包含 id 和 name 字段。</p>
     */
    private List<Map<String, Object>> pendingApprovers;
}
