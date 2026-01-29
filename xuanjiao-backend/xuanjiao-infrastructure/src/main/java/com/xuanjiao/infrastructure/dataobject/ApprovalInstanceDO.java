package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("approval_instance")
public class ApprovalInstanceDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workflowId;
    private String businessType;
    private Long businessId;
    private Long applicantId;
    private Long currentStageId;
    private String status;
    private Long parentInstanceId; // 父实例ID（用于子流程关联）
    private Long parentTaskId; // 父任务ID（用于子流程，记录是哪个任务触发的）
    private Long rootInstanceId; // 根实例ID（用于追溯主流程）
    private String subWorkflowApproverIds; // 子流程第一层审批人IDs（JSON格式，key为子流程ID，value为审批人ID列表）
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
