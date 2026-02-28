package com.xuanjiao.client.workflow;

import lombok.Data;
import java.util.List;

/**
 * 工作流数据传输对象
 *
 * <p>用于在前后端之间传输工作流定义信息，包括工作流基本信息、
 * 绑定的角色、流程类型和各阶段配置。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowDTO {

    /**
     * 工作流ID
     */
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
     * 版本号
     */
    private Integer version;

    /**
     * 工作流状态（1-启用、0-禁用）
     */
    private Integer status;

    /**
     * 绑定的角色ID（一个流程对应一个角色）
     */
    private Long boundRoleId;

    /**
     * 绑定的角色名称
     */
    private String roleName;

    /**
     * 流程类型
     * <ul>
     *   <li>ASSET_UPLOAD - 素材录入审批</li>
     *   <li>ASSET_USAGE - 素材使用审批</li>
     *   <li>ASSET_DELETION - 素材删除审批</li>
     * </ul>
     */
    private String workflowType;

    /**
     * 工作流阶段列表
     */
    private List<WorkflowStageDTO> stages;
}
