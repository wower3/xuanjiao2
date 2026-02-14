package com.xuanjiao.client;

import lombok.Data;
import java.util.List;

/**
 * 角色数据传输对象
 *
 * <p>用于在前后端之间传输角色信息，包括角色基本信息、
 * 部门权限范围、角色类型和关联的菜单列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class RoleDTO {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 部门权限范围（ALL-全部部门、DEPT-本部门、DEPT_AND_SUB-本部门及下级）
     */
    private String deptScope;

    /**
     * 角色类型（ADMIN-管理员、APPROVER-审批人、USER-普通用户）
     */
    private String roleType;

    /**
     * 角色状态（1-启用、0-禁用）
     */
    private Integer status;

    /**
     * 角色关联的菜单ID列表
     */
    private List<Long> menuIds;
}
