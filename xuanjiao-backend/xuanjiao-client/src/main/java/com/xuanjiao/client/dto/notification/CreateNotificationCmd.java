package com.xuanjiao.client.dto.notification;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class CreateNotificationCmd {
    private String title;
    private String content;
    private String notificationType;
    private String sourceType;
    private Long sourceId;
    @NotEmpty(message = "recipients cannot be empty")
    private List<Long> recipientIds;
}
