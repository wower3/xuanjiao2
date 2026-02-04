package com.xuanjiao.domain.material.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MaterialApplication {
    private Long id;
    private String title;
    private Long applicantId;
    private Long maintainerId;
    private Long deptId;
    private Long workflowId;
    private String status;
    private Integer guaranteeDeclaration;
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
