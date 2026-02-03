package com.xuanjiao.client.dto.workflow;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 绑定角色命令对象
 */
@Data
public class WorkflowBindRoleCmd {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @NotNull(message = "工作流类型不能为空")
    private String workflowType;
}
