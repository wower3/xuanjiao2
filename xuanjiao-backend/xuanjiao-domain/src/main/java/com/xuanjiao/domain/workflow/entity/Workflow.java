package com.xuanjiao.domain.workflow.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作流定义实体
 * <p>定义审批流程的结构，包括流程名称、版本、状态以及包含的阶段列表</p>
 * <p>一个工作流定义可以创建多个审批实例</p>
 * <p>支持版本管理，通过version字段区分不同版本</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.WorkflowDO
 */
@Data
public class Workflow {
    /** 工作流唯一标识，自增主键 */
    private Long id;

    /** 工作流名称，如"素材录入审批流程"、"素材使用审批流程" */
    private String name;

    /** 工作流描述，说明工作流的用途和适用范围 */
    private String description;

    /** 版本号，用于工作流的版本管理，数值越大版本越新 */
    private Integer version;

    /** 工作流状态：1-启用、0-停用，只有启用状态的工作流可以创建审批实例 */
    private Integer status;

    /** 流程类型：ASSET_UPLOAD-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除 */
    private String type;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 工作流阶段列表，包含该工作流的所有审批阶段 */
    private List<WorkflowStage> stages;
}
