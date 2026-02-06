package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批实例数据对象
 * <p>对应数据库表 approval_instance，存储审批实例的持久化数据</p>
 * <p>支持主流程和子流程：子流程通过parentInstanceId关联父实例</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.approval.entity.ApprovalInstance
 */
@Data
@TableName("approval_instance")
public class ApprovalInstanceDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的工作流定义ID */
    private Long workflowId;

    /** 业务类型：MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除 */
    private String businessType;

    /** 关联的业务记录ID */
    private Long businessId;

    /** 申请人ID */
    private Long applicantId;

    /** 当前所在阶段ID */
    private Long currentStageId;

    /** 审批实例状态：PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝、CANCELLED-已取消 */
    private String status;

    /** 父实例ID（用于子流程关联） */
    private Long parentInstanceId;

    /** 父任务ID（用于子流程，记录是哪个任务触发的） */
    private Long parentTaskId;

    /** 根实例ID（用于追溯主流程） */
    private Long rootInstanceId;

    /** 子流程第一层审批人IDs，JSON格式（key为子流程ID，value为审批人ID列表） */
    private String subWorkflowApproverIds;

    /** 创建时间，自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
