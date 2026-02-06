package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 工作流阶段数据对象
 * <p>对应数据库表 workflow_stage，存储工作流阶段的持久化数据</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.workflow.entity.WorkflowStage
 */
@Data
@TableName("workflow_stage")
public class WorkflowStageDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的工作流定义ID */
    private Long workflowId;

    /** 阶段名称 */
    private String name;

    /** 阶段顺序 */
    private Integer stageOrder;

    /** 审批类型：AND-会签、OR-或签 */
    private String approveType;

    /** 逻辑删除标识 */
    @TableLogic
    private Integer deleted;
}
