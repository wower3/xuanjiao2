package com.xuanjiao.client.dto.workflow;

import lombok.Data;

/**
 * 创建工作流命令对象
 */
@Data
public class WorkflowCreateCmd {

    private String name;

    private String description;

    private String workflowType;
}
