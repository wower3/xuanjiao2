package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("approval_task")
public class ApprovalTaskDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instanceId;
    private Long stageId;
    private Long approverId;
    private String status;
    private String comment;
    private LocalDateTime approveTime;
    private String nextStageApproverIds; // 下一层审批人IDs（JSON格式）
    private String subWorkflowApproverIds; // 子流程第一层审批人IDs（JSON格式，key为子流程ID，value为审批人ID列表）
    private Long selectedByUserId; // 选择下一层审批人的用户ID
    private Integer isFirstApprover; // 是否是该阶段第一个审批人：0-否，1-是
    private String taskType; // 任务类型：NORMAL-普通审批任务，RESTART_SUB_WORKFLOW-重新发起子流程
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
