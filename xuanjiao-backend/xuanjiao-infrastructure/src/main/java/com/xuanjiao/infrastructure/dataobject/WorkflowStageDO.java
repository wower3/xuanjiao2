package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("workflow_stage")
public class WorkflowStageDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workflowId;
    private String name;
    private Integer stageOrder;
    private String approveType;
    @TableLogic
    private Integer deleted;
}
