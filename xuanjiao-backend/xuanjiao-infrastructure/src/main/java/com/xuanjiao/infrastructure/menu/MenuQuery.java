package com.xuanjiao.infrastructure.menu;

import lombok.Data;

/**
 * Menu Query Object
 * Dynamic query parameters for MenuMapper
 */
@Data
public class MenuQuery {
    private Long id;
    private Long parentId;
    private String name;
    private String type;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Integer status;
    private Integer deleted;
    private String orderByField;
    private String orderByDirection;
}
