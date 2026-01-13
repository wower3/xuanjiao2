package com.xuanjiao.client.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoleDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String deptScope;
    private String roleType;
    private Integer status;
    private List<Long> menuIds;  // 角色关联的菜单ID列表
}
