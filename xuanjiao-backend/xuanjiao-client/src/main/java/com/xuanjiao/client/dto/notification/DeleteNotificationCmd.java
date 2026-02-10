package com.xuanjiao.client.dto.notification;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除通知命令对象
 *
 * <p>用于删除指定的通知。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class DeleteNotificationCmd {

    /**
     * 通知ID
     */
    @NotNull(message = "id cannot be null")
    private Long id;
}
