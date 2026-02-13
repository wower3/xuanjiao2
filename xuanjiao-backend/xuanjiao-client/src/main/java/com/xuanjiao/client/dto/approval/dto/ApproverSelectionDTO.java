package com.xuanjiao.client.dto.approval.dto;

import lombok.Data;

/**
 * 审批人选择数据传输对象
 *
 * <p>用于在审批人选择界面展示可选审批人的信息，
 * 包括用户基本信息、所属部门和角色等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApproverSelectionDTO {

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
     * 部门名称
     */
    private String deptName;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 角色ID
     */
    private Long roleId;
}
