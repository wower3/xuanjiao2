package com.xuanjiao.client.dto.workflow;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 解除角色绑定命令对象
 */
@Data
public class WorkflowUnbindRoleCmd {

    @NotNull(message = "ID不能为空")
    private Long id;
}
