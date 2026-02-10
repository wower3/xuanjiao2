package com.xuanjiao.client.dto;

import lombok.Data;

/**
 * 用户数据传输对象
 *
 * <p>用于在前后端之间传输用户信息，包括用户基本信息、
 * 所属部门、角色等完整属性。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UserDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 所属部门名称
     */
    private String deptName;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色类型（ADMIN-管理员、APPROVER-审批人、USER-普通用户）
     */
    private String roleType;

    /**
     * 用户状态（1-启用、0-禁用）
     */
    private Integer status;
}
