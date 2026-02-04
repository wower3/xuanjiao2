package com.xuanjiao.infrastructure.workflow;

import lombok.Data;

import java.util.List;

/**
 * Workflow Query Object
 * Dynamic query parameters for WorkflowMapper
 */
@Data
public class WorkflowQuery {
    private Long id;
    private String name;
    private Integer version;
    private Integer status;
    private Long boundRoleId;
    private String workflowType;
    private Integer deleted;
    private List<Long> excludeIds; // For ne(id) queries
    private String orderByField;
    private String orderByDirection;
}
