package com.xuanjiao.domain.workflow.entity;

import lombok.Data;
import java.util.List;

/**
 * 工作流阶段实体
 *
 * <p>定义工作流中的一个审批阶段，包含阶段名称、审批类型和审批人列表。</p>
 * <p>工作流由多个阶段组成，阶段之间顺序执行。</p>
 * <p>支持会签（AND）和或签（OR）两种审批模式。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowStage {

    /**
     * 阶段唯一标识
     *
     * <p>自增主键。</p>
     */
    private Long id;

    /**
     * 关联的工作流定义ID
     *
     * <p>指向Workflow。</p>
     */
    private Long workflowId;

    /**
     * 阶段名称
     *
     * <p>如"部门审批"、"领导审批"、"综合审批"等。</p>
     */
    private String name;

    /**
     * 阶段排序序号
     *
     * <p>决定阶段的执行顺序，数值越小越先执行。</p>
     */
    private Integer stageOrder;

    /**
     * 审批类型
     *
     * <p>AND-会签（所有审批人需同意）、OR-或签（任一审批人同意即可）。</p>
     */
    private String approveType;

    /**
     * 阶段审批人列表
     *
     * <p>定义该阶段有哪些审批人/审批角色。</p>
     */
    private List<StageApprover> approvers;
}
