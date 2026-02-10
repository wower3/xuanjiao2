package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 工作流阶段数据对象
 *
 * <p>映射数据库 workflow_stage 表，用于 MyBatis 数据访问。</p>
 * <p>存储工作流阶段信息，包括阶段名称、顺序、审批类型等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("workflow_stage")
public class WorkflowStageDO {

    /**
     * 阶段ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的工作流定义ID
     */
    private Long workflowId;

    /**
     * 阶段名称
     */
    private String name;

    /**
     * 阶段顺序
     */
    private Integer stageOrder;

    /**
     * 审批类型：AND-会签（所有审批人需同意）、OR-或签（任一审批人同意即可）
     */
    private String approveType;

    /**
     * 逻辑删除标识：0-未删除、1-已删除
     */
    @TableLogic
    private Integer deleted;
}
