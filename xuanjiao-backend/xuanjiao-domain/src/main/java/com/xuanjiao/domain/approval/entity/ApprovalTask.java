package com.xuanjiao.domain.approval.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批任务实体
 *
 * <p>代表审批流程中的一个具体任务，分配给审批人执行。</p>
 * <p>一个审批阶段可能有多个任务（并行审批），任务状态独立管理。</p>
 * <p>支持会签（AND）和或签（OR）两种审批模式。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalTask {

    /**
     * 审批任务唯一标识
     *
     * <p>自增主键。</p>
     */
    private Long id;

    /**
     * 关联的审批实例ID
     *
     * <p>指向ApprovalInstance。</p>
     */
    private Long instanceId;

    /**
     * 关联的工作流阶段ID
     *
     * <p>指向workflow_stage表。</p>
     */
    private Long stageId;

    /**
     * 审批人ID
     *
     * <p>关联sys_user表，负责处理该任务的用户。</p>
     */
    private Long approverId;

    /**
     * 任务状态
     *
     * <p>PENDING-待处理、APPROVED-已通过、REJECTED-已拒绝、RETURNED-已退回、CANCELLED-已取消。</p>
     */
    private String status;

    /**
     * 审批意见/评论
     *
     * <p>审批人填写的审批说明。</p>
     */
    private String comment;

    /**
     * 审批时间
     *
     * <p>审批人处理任务的时间。</p>
     */
    private LocalDateTime approveTime;

    /**
     * 下一阶段审批人ID列表
     *
     * <p>JSON格式，当前审批人选择的下阶段审批人。</p>
     */
    private String nextStageApproverIds;

    /**
     * 子流程审批人ID列表
     *
     * <p>JSON格式，当前审批人选择的子流程审批人。</p>
     */
    private String subWorkflowApproverIds;

    /**
     * 选择该审批人的用户ID
     *
     * <p>指派该任务给审批人的操作人。</p>
     */
    private Long selectedByUserId;

    /**
     * 是否为首个审批人
     *
     * <p>1-是、0-否，用于或签场景确定首个处理人。</p>
     */
    private Integer isFirstApprover;

    /**
     * 创建时间
     *
     * <p>任务生成的时间。</p>
     */
    private LocalDateTime createTime;
}
