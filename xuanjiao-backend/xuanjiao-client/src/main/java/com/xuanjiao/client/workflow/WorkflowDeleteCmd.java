package com.xuanjiao.client.workflow;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除工作流命令对象
 *
 * <p>用于删除指定的工作流。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowDeleteCmd {

    /**
     * 工作流ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;
}
