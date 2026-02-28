package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批任务数据对象
 *
 * <p>映射数据库 approval_task 表，用于 MyBatis 数据访问。</p>
 * <p>存储审批任务信息，一个审批阶段可能有多个任务（并行审批场景）。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("approval_task")
public class ApprovalTaskDO {

    /**
     * 任务ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的审批实例ID
     */
    private Long instanceId;

    /**
     * 关联的工作流阶段ID
     */
    private Long stageId;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 任务状态：PENDING-待处理、APPROVED-已通过、REJECTED-已拒绝、RETURNED-已退回、CANCELLED-已取消
     */
    private String status;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 下一层审批人IDs，JSON格式
     */
    private String nextStageApproverIds;

    /**
     * 子流程第一层审批人IDs，JSON格式（key为子流程ID，value为审批人ID列表）
     */
    private String subWorkflowApproverIds;

    /**
     * 选择下一层审批人的用户ID
     */
    private Long selectedByUserId;

    /**
     * 是否是该阶段第一个审批人：0-否、1-是（用于或签场景）
     */
    private Integer isFirstApprover;

    /**
     * 任务类型：NORMAL-普通审批任务、RESTART_SUB_WORKFLOW-重新发起子流程
     */
    private String taskType;

    /**
     * 创建时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
