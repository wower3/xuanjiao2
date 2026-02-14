package com.xuanjiao.client.notification;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 标记通知已读命令对象
 *
 * <p>用于将指定通知标记为已读状态。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MarkReadCmd {

    /**
     * 通知ID
     */
    @NotNull(message = "id cannot be null")
    private Long id;
}
