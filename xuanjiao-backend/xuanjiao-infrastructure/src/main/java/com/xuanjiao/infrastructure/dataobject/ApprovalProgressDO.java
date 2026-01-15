package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("approval_progress")
public class ApprovalProgressDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long instanceId; // 审批实例ID
    private Long stageId; // 阶段ID
    private String stageName; // 阶段名称
    private Integer stageOrder; // 阶段顺序
    private String status; // 状态：PENDING-待审批，APPROVED-已通过，REJECTED-已驳回，SKIPPED-已跳过
    private String approvers; // 审批人列表（JSON格式：[{id, name, status, approveTime}]）
    private Integer isSubWorkflow; // 是否是子流程：0-否，1-是
    private Long parentInstanceId; // 父实例ID（用于子流程）
    private Long parentTaskId; // 父任务ID（用于子流程，记录是哪个任务触发的子流程）
    private LocalDateTime approveTime; // 审批通过时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
