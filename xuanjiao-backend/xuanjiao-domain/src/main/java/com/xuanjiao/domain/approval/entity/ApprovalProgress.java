package com.xuanjiao.domain.approval.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批进度实体
 *
 * <p>记录审批流程的阶段性进展，一个审批实例有多个进度记录。</p>
 * <p>用于前端展示审批流程图，直观显示各阶段的审批情况。</p>
 * <p>支持子流程进度展示，通过parentInstanceId关联主流程。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalProgress {

    /**
     * 审批进度唯一标识
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
     * 阶段名称
     *
     * <p>用于展示，如"部门审批"、"领导审批"等。</p>
     */
    private String stageName;

    /**
     * 阶段排序序号
     *
     * <p>决定阶段的执行顺序。</p>
     */
    private Integer stageOrder;

    /**
     * 进度状态
     *
     * <p>PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝。</p>
     */
    private String status;

    /**
     * 审批人信息
     *
     * <p>JSON格式，存储阶段内所有审批人的处理情况。</p>
     */
    private String approvers;

    /**
     * 是否为子流程
     *
     * <p>1-是、0-否，子流程独立记录进度。</p>
     */
    private Integer isSubWorkflow;

    /**
     * 父审批实例ID
     *
     * <p>子流程特有，指向主流程的审批实例。</p>
     */
    private Long parentInstanceId;

    /**
     * 父审批任务ID
     *
     * <p>子流程特有，指向触发子流程的任务。</p>
     */
    private Long parentTaskId;

    /**
     * 审批完成时间
     *
     * <p>该阶段所有审批处理完成的时间。</p>
     */
    private LocalDateTime approveTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
