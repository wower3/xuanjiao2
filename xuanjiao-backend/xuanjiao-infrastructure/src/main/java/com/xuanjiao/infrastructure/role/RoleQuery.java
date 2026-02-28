package com.xuanjiao.infrastructure.role;

import lombok.Data;

import java.util.List;

/**
 * 角色查询条件对象
 *
 * <p>用于动态构建角色查询条件，支持多种查询方式。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class RoleQuery {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色类型（用于精确查询）
     */
    private String roleType;

    /**
     * 角色类型列表（IN查询）
     */
    private List<String> roleTypes;

    /**
     * 角色状态（0-禁用、1-启用）
     */
    private Integer status;

    /**
     * 删除标记（0-未删除、1-已删除）
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
