package com.xuanjiao.infrastructure.workflow;

import lombok.Data;

import java.util.List;

/**
 * 工作流查询条件对象
 *
 * <p>用于动态构建工作流查询条件，对应 WorkflowMapper 使用。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowQuery {

    /**
     * 工作流ID
     */
    private Long id;

    /**
     * 工作流名称（模糊查询）
     */
    private String name;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 工作流状态（1-启用、0-停用）
     */
    private Integer status;

    /**
     * 绑定的角色ID
     */
    private Long boundRoleId;

    /**
     * 工作流类型（ASSET_UPLOAD, ASSET_USAGE, ASSET_DELETION）
     */
    private String workflowType;

    /**
     * 删除标记
     */
    private Integer deleted;

    /**
     * 排除的ID列表（用于 ne(id) 查询）
     */
    private List<Long> excludeIds;

    /**
     * 排序字段
     */
    private String orderByField;

    /**
     * 排序方向（ASC/DESC）
     */
    private String orderByDirection;
}
