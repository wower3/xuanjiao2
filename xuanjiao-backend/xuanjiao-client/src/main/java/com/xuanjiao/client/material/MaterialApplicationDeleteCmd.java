package com.xuanjiao.client.material;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除素材申请单命令对象
 *
 * <p>用于删除指定的素材录入申请，仅草稿状态可删除。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MaterialApplicationDeleteCmd {

    /**
     * 申请单ID
     */
    @NotNull(message = "申请单ID不能为空")
    private Long id;
}
