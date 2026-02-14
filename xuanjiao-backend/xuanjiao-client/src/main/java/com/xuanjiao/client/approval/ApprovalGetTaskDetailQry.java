package com.xuanjiao.client.approval;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取审批任务详情查询对象
 *
 * <p>用于根据任务ID获取审批任务的详细信息，包括任务状态、
 * 审批进度和关联的业务数据。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalGetTaskDetailQry {

    /**
     * 审批任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long id;
}
