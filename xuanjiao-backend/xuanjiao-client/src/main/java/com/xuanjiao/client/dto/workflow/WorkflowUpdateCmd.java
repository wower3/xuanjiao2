package com.xuanjiao.client.dto.workflow;

import com.xuanjiao.client.dto.WorkflowStageDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新工作流命令对象
 *
 * <p>封装更新工作流所需的参数信息，包括阶段配置。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowUpdateCmd {

    /**
     * 工作流ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;

    /**
     * 工作流名称
     */
    private String name;

    /**
     * 工作流描述
     */
    private String description;

    /**
     * 流程类型
     */
    private String workflowType;

    /**
     * 工作流阶段配置列表
     */
    private List<WorkflowStageDTO> stages;
}
