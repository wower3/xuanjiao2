package com.xuanjiao.client.workflow;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 绑定角色命令对象
 *
 * <p>用于将工作流绑定到指定角色，一个工作流只能绑定一个角色。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowBindRoleCmd {

    /**
     * 工作流ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /**
     * 工作流类型
     */
    @NotNull(message = "工作流类型不能为空")
    private String workflowType;
}
