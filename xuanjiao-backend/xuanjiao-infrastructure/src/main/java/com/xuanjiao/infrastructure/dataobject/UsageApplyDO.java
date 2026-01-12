package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("usage_apply")
public class UsageApplyDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private Long userId;
    private String purpose;
    private String scope;
    private Long workflowId;
    private String status;
    private Long approvalInstanceId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
