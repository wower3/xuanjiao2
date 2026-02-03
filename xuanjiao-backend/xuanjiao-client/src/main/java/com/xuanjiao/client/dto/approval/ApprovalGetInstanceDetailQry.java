package com.xuanjiao.client.dto.approval;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取审批实例详情查询对象
 */
@Data
public class ApprovalGetInstanceDetailQry {

    @NotNull(message = "实例ID不能为空")
    private Long id;
}
