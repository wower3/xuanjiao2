package com.xuanjiao.client.dto;

import lombok.Data;

@Data
public class ApproverSelectionDTO {
    private Long id; // 用户ID
    private String username; // 用户名
    private String realName; // 姓名
    private String deptName; // 部门名称
    private String roleName; // 角色名称
    private Long deptId; // 部门ID
    private Long roleId; // 角色ID
}
