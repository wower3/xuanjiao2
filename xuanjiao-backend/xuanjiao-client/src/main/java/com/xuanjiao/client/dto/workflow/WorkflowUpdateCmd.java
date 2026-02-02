package com.xuanjiao.client.dto.workflow;

import com.xuanjiao.client.dto.WorkflowStageDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 更新工作流命令对象
 */
@Data
public class WorkflowUpdateCmd {

    @NotNull(message = "ID不能为空")
    private Long id;

    private String name;

    private String description;

    private String workflowType;

    private List<WorkflowStageDTO> stages;
}
