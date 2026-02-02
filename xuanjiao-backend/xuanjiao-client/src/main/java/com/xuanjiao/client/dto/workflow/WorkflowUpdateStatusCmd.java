package com.xuanjiao.client.dto.workflow;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 更新工作流状态命令对象
 */
@Data
public class WorkflowUpdateStatusCmd {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
