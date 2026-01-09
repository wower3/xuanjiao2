package com.xuanjiao.client.dto;

import lombok.Data;

@Data
public class RoleDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer status;
}
