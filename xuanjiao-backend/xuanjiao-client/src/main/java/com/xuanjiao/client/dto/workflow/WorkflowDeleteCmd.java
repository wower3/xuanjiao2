package com.xuanjiao.client.dto.workflow;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除工作流命令对象
 */
@Data
public class WorkflowDeleteCmd {

    @NotNull(message = "ID不能为空")
    private Long id;
}
