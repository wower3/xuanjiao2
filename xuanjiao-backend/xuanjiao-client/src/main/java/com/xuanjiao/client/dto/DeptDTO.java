package com.xuanjiao.client.dto;

import lombok.Data;

@Data
public class DeptDTO {
    private Long id;
    private String name;
    private Long parentId;
    private Long leaderId;
    private Integer sort;
    private Integer status;
}
