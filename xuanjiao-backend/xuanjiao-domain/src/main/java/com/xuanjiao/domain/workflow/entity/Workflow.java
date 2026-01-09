package com.xuanjiao.domain.workflow.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Workflow {
    private Long id;
    private String name;
    private String description;
    private Integer version;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<WorkflowStage> stages;
}
