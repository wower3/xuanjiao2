package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("workflow")
public class WorkflowDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer version;
    private Integer status;
    private Long boundRoleId; // 绑定的角色ID（一个流程对应一个角色）
    private String workflowType; // 流程类型：ASSET_UPLOAD-素材录入审批，ASSET_USAGE-素材使用审批
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
