package com.xuanjiao.client.dto.asset;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除标签命令对象
 *
 * <p>用于删除指定的标签。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class TagDeleteCmd {

    /**
     * 标签ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;
}
