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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
