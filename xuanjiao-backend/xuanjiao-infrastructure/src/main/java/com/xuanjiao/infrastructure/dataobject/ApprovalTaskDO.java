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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
