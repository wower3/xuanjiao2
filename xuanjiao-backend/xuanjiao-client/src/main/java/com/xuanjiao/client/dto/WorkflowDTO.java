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
    private Long boundRoleId; // 绑定的角色ID（一个流程对应一个角色）
    private String roleName; // 绑定的角色名称
    private String workflowType; // 流程类型：ASSET_UPLOAD-素材录入审批，ASSET_USAGE-素材使用审批
    private List<WorkflowStageDTO> stages;
}
