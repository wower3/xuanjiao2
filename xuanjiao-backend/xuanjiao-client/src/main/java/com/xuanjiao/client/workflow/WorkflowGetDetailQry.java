package com.xuanjiao.client.workflow;

import lombok.Data;

/**
 * 获取工作流详情查询对象
 *
 * <p>用于根据工作流ID获取工作流的详细信息，包括各阶段配置。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowGetDetailQry {

    /**
     * 工作流ID
     */
    private Long id;
}
