package com.xuanjiao.client.dto.approval;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取审批任务详情查询对象
 */
@Data
public class ApprovalGetTaskDetailQry {

    @NotNull(message = "任务ID不能为空")
    private Long id;
}
