package com.xuanjiao.client.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorkflowDTO {
    private Long id;
    private String name;
    private String description;
    private Integer version;
    private Integer status;
    private String type; // 流程类型：ASSET_UPLOAD-素材录入, ASSET_USAGE-素材使用
    private List<WorkflowStageDTO> stages;
}
