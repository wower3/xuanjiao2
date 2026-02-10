package com.xuanjiao.client.dto.notification;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取通知详情查询对象
 *
 * <p>用于根据通知ID获取通知的详细信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class NotificationGetDetailQry {

    /**
     * 通知ID
     */
    @NotNull(message = "id cannot be null")
    private Long id;
}
