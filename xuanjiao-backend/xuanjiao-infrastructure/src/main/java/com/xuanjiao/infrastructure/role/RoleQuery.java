package com.xuanjiao.infrastructure.role;

import lombok.Data;

/**
 * 角色查询条件对象
 * 用于动态构建查询条件
 */
@Data
public class RoleQuery {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 角色类型（用于精确查询）
     */
    private String roleType;

    /**
     * 角色类型列表（IN查询）
     */
    private java.util.List<String> roleTypes;

    /**
     * 角色状态（0:禁用, 1:启用）
     */
    private Integer status;

    /**
     * 删除标记（0:未删除, 1:已删除）
     * 默认查询未删除的记录
     */
    private Integer deleted;

    /**
     * 角色名称关键字（模糊查询）
     */
    private String nameKeyword;

    /**
     * 排除的ID（用于唯一性检查）
     */
    private Long excludeId;

    /**
     * 排序字段（默认 id DESC）
     */
    private String orderByField = "id";

    /**
     * 排序方向（ASC 或 DESC）
     */
    private String orderByDirection = "DESC";
}
