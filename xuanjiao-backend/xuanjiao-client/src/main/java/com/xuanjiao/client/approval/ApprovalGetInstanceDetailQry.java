package com.xuanjiao.client.approval;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取审批实例详情查询对象
 *
 * <p>用于根据实例ID获取审批实例的详细信息，包括实例状态、
 * 关联的业务数据和完整的审批进度。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalGetInstanceDetailQry {

    /**
     * 审批实例ID
     */
    @NotNull(message = "实例ID不能为空")
    private Long id;
}
