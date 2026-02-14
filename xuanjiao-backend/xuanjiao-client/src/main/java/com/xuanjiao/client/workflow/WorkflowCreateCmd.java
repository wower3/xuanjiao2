package com.xuanjiao.client.workflow;

import lombok.Data;

/**
 * 创建工作流命令对象
 *
 * <p>封装创建工作流所需的基本参数信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowCreateCmd {

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
     * <ul>
     *   <li>ASSET_UPLOAD - 素材录入审批</li>
     *   <li>ASSET_USAGE - 素材使用审批</li>
     *   <li>ASSET_DELETION - 素材删除审批</li>
     * </ul>
     */
    private String workflowType;
}
