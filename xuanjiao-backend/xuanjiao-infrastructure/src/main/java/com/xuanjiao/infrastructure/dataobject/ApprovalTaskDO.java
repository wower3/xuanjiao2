package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批任务数据对象
 * <p>对应数据库表 approval_task，存储审批任务的持久化数据</p>
 * <p>一个审批阶段可能有多个任务（并行审批）</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.approval.entity.ApprovalTask
 */
@Data
@TableName("approval_task")
public class ApprovalTaskDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的审批实例ID */
    private Long instanceId;

    /** 关联的工作流阶段ID */
    private Long stageId;

    /** 审批人ID */
    private Long approverId;

    /** 任务状态：PENDING-待处理、APPROVED-已通过、REJECTED-已拒绝、RETURNED-已退回、CANCELLED-已取消 */
    private String status;

    /** 审批意见 */
    private String comment;

    /** 审批时间 */
    private LocalDateTime approveTime;

    /** 下一层审批人IDs，JSON格式 */
    private String nextStageApproverIds;

    /** 子流程第一层审批人IDs，JSON格式（key为子流程ID，value为审批人ID列表） */
    private String subWorkflowApproverIds;

    /** 选择下一层审批人的用户ID */
    private Long selectedByUserId;

    /** 是否是该阶段第一个审批人：0-否，1-是（用于或签场景） */
    private Integer isFirstApprover;

    /** 任务类型：NORMAL-普通审批任务，RESTART_SUB_WORKFLOW-重新发起子流程 */
    private String taskType;

    /** 创建时间，自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
