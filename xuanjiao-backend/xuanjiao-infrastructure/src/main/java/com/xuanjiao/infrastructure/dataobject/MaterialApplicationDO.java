package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("material_application")
public class MaterialApplicationDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private Long applicantId;
    private Long maintainerId;
    private Long deptId;
    private Long workflowId;
    private String status;
    private Integer guaranteeDeclaration;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
