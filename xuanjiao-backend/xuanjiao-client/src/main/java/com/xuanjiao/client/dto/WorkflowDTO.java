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
    private List<WorkflowStageDTO> stages;
}
