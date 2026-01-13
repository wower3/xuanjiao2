package com.xuanjiao.client.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private Long deptId;
    private String deptName;
    private Long roleId;
    private String roleName;
    private String roleType;
    private Integer status;
}
