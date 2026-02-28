package com.xuanjiao.client.workflow;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 解除角色绑定命令对象
 *
 * <p>用于解除工作流与角色的绑定关系。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowUnbindRoleCmd {

    /**
     * 工作流ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;
}
