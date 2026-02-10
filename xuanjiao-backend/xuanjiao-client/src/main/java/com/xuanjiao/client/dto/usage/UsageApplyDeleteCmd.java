package com.xuanjiao.client.dto.usage;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除使用申请命令对象
 *
 * <p>用于删除指定的素材使用申请，仅草稿状态可删除。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UsageApplyDeleteCmd {

    /**
     * 申请单ID
     */
    @NotNull(message = "申请单ID不能为空")
    private Long id;
}
