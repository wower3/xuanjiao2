package com.xuanjiao.client.dto.material;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 素材录入申请提交审批命令
 *
 * <p>用于将草稿状态的素材录入申请提交到指定的审批流程。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MaterialApplicationSubmitCmd {

    /**
     * 申请单ID
     */
    @NotNull(message = "申请单ID不能为空")
    private Long id;

    /**
     * 审批流程ID
     */
    @NotNull(message = "审批流程ID不能为空")
    private Long workflowId;
}
