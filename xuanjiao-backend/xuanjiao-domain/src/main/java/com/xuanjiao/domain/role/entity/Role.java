package com.xuanjiao.domain.role.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色实体
 * <p>代表系统中的角色，用于用户权限管理</p>
 * <p>角色关联菜单权限，用户通过角色获得权限</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.RoleDO
 */
@Data
public class Role {
    /** 角色唯一标识，自增主键 */
    private Long id;

    /** 角色名称 */
    private String name;

    /** 角色描述 */
    private String description;

    /** 部门范围：ALL-全部、SELF-本部门、SELF_AND_SUB-本部门及子部门 */
    private String deptScope;

    /** 角色类型：ADMIN-管理员、APPROVER-审批人、USER-普通用户 */
    private String roleType;

    /** 角色状态：1-正常、0-停用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
