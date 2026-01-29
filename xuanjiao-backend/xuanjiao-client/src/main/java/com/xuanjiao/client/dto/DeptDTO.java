package com.xuanjiao.client.dto;

import lombok.Data;
import java.util.List;

@Data
public class DeptDTO {
    private Long id;
    private String code;
    private Integer level;
    private String fullCode;
    private String name;
    private Long parentId;
    private Long leaderId;
    private Integer sort;
    private Integer status;
    private List<DeptDTO> children;
}
