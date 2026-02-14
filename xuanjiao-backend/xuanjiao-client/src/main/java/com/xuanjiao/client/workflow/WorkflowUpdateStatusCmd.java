package com.xuanjiao.client.workflow;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 更新工作流状态命令对象
 *
 * <p>用于启用或禁用工作流。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowUpdateStatusCmd {

    /**
     * 工作流ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;

    /**
     * 状态（1-启用、0-禁用）
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
