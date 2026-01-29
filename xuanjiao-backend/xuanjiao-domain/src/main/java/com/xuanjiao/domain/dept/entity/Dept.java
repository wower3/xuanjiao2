package com.xuanjiao.domain.dept.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Dept {
    private Long id;
    private String code;
    private Integer level;
    private String fullCode;
    private String name;
    private Long parentId;
    private Long leaderId;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
