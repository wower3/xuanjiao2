package com.xuanjiao.infrastructure.user;

import lombok.Data;

import java.util.List;

/**
 * 用户查询条件对象
 *
 * <p>用于动态构建用户查询条件，支持多种查询方式。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UserQuery {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门ID列表（IN查询）
     */
    private List<Long> deptIds;

    /**
     * 用户状态（0-禁用、1-启用）
     */
    private Integer status;

    /**
     * 删除标记（0-未删除、1-已删除）
     */
    private Integer deleted;

    /**
     * 用户名关键字（模糊查询）
     */
    private String usernameKeyword;

    /**
     * 真实姓名关键字（模糊查询）
     */
    private String realNameKeyword;

    /**
     * 通用关键字（同时搜索username和realName）
     */
    private String keyword;

    /**
     * 用户ID列表（IN查询）
     */
    private List<Long> userIds;

    /**
     * 角色ID列表（IN查询）
     */
    private List<Long> roleIds;

    /**
     * 子部门ID列表（用于递归查询子部门下的用户）
     */
    private List<Long> subDeptIds;
}
