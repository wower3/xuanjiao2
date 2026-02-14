package com.xuanjiao.client.deletion;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除删除申请命令对象
 *
 * <p>用于删除指定的素材删除申请，仅草稿状态可删除。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class DeletionDeleteCmd {

    /**
     * 申请单ID
     */
    @NotNull(message = "申请单ID不能为空")
    private Long id;
}
