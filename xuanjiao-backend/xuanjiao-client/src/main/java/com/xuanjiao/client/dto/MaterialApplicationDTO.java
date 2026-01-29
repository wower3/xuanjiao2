package com.xuanjiao.client.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MaterialApplicationDTO {
    private Long id;
    private String title;
    private Long applicantId;
    private String applicantName;
    private Long maintainerId;
    private String maintainerName;
    private Long deptId;
    private String deptName;
    private Long workflowId;
    private String status;
    private Integer guaranteeDeclaration;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<AssetDTO> assets;
}
