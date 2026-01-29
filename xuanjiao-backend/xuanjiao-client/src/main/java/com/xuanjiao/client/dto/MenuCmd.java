package com.xuanjiao.client.dto;

import lombok.Data;

@Data
public class MenuCmd {
    private Long id;
    private Long parentId;
    private String name;
    private String type;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Integer status;
}
