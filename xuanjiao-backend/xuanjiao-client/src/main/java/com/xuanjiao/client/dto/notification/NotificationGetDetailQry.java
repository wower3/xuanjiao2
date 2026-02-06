package com.xuanjiao.client.dto.notification;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class NotificationGetDetailQry {
    @NotNull(message = "id cannot be null")
    private Long id;
}
