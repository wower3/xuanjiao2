package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("stage_approver")
public class StageApproverDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long stageId;
    private String approverType;
    private Long approverId;
}
