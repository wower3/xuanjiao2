package com.xuanjiao.infrastructure.material;

import lombok.Data;

import java.util.List;

/**
 * 素材申请查询条件对象
 * 用于动态构建查询条件
 */
@Data
public class MaterialApplicationQuery {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 维护人ID
     */
    private Long maintainerId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 状态（DRAFT, PENDING, APPROVED, REJECTED, RETURNED）
     */
    private String status;

    /**
     * 删除标记（0:未删除, 1:已删除）
     * 默认查询未删除的记录
     */
    private Integer deleted;

    /**
     * 排序字段
     */
    private String orderByField;

    /**
     * 排序方向（ASC, DESC）
     */
    private String orderByDirection;

    /**
     * 申请人ID列表（IN查询）
     */
    private List<Long> applicantIds;
}
