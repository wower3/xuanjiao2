package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("usage_apply")
public class UsageApplyDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;          // 申请单标题
    private String purpose;
    private String scope;
    private Long workflowId;
    private String status;
    private Long approvalInstanceId;

    // 新增字段
    private String attachmentPath;       // 附件路径
    private Integer isSecondaryCreation; // 是否二次创作:0-否,1-是
    private String publishChannel;       // 发布渠道
    private Long deptId;                 // 申请部门ID
    private Integer draft;               // 是否草稿:0-已提交,1-草稿

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
