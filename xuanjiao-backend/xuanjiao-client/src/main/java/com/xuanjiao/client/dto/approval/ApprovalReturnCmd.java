package com.xuanjiao.client.dto.approval;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotNull;

/**
 * 退回审批任务命令
 *
 * <p>用于将审批任务退回到上一阶段，同时记录退回原因。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalReturnCmd {

    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    /**
     * 退回原因
     */
    private String comment;
}
