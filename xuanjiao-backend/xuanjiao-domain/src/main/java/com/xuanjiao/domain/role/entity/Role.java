package com.xuanjiao.domain.role.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Role {
    private Long id;
    private String name;
    private String description;
    private String deptScope;
    private String roleType;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
