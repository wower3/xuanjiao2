package com.xuanjiao.client.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuDTO {
    private Long id;
    private Long parentId;
    private String name;
    private String type;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<MenuDTO> children;
}
