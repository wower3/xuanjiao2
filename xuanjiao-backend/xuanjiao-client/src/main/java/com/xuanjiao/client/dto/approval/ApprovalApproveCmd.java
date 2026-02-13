package com.xuanjiao.client.dto.approval;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotNull;

/**
 * 审批操作命令
 *
 * <p>用于执行审批通过或驳回操作，记录审批意见。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalApproveCmd {

    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 是否通过（true-通过，false-驳回）
     */
    @NotNull(message = "审批结果不能为空")
    private Boolean passed;
}
