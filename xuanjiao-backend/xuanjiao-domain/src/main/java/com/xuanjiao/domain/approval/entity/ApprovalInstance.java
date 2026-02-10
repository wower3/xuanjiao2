package com.xuanjiao.domain.approval.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批实例实体
 *
 * <p>代表一次完整的审批流程实例，记录审批的基本信息和当前状态。</p>
 * <p>支持主流程和子流程：子流程通过parentInstanceId关联父实例。</p>
 * <p>状态流转：PENDING → APPROVED/REJECTED，CANCELLED（取消）。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalInstance {

    /**
     * 审批实例唯一标识
     *
     * <p>自增主键。</p>
     */
    private Long id;

    /**
     * 关联的工作流定义ID
     *
     * <p>定义审批的流程结构，指向workflow表。</p>
     */
    private Long workflowId;

    /**
     * 业务类型
     *
     * <p>MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除。</p>
     */
    private String businessType;

    /**
     * 关联的业务记录ID
     *
     * <p>如素材录入申请ID、素材使用申请ID等。</p>
     */
    private Long businessId;

    /**
     * 申请人ID
     *
     * <p>关联sys_user表，发起审批的用户。</p>
     */
    private Long applicantId;

    /**
     * 当前所在阶段ID
     *
     * <p>关联workflow_stage表，表示当前审批进行到的阶段。</p>
     */
    private Long currentStageId;

    /**
     * 审批实例状态
     *
     * <p>PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝、CANCELLED-已取消。</p>
     */
    private String status;

    /**
     * 父审批实例ID
     *
     * <p>子流程特有，指向创建该子流程的父审批实例。</p>
     */
    private Long parentInstanceId;

    /**
     * 父审批任务ID
     *
     * <p>子流程特有，指向触发子流程的父审批任务。</p>
     */
    private Long parentTaskId;

    /**
     * 根审批实例ID
     *
     * <p>子流程特有，指向最顶层的主审批实例，用于追踪整个流程链。</p>
     */
    private Long rootInstanceId;

    /**
     * 子流程审批人ID列表
     *
     * <p>JSON格式，存储子流程各阶段选择的审批人ID。</p>
     */
    private String subWorkflowApproverIds;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
