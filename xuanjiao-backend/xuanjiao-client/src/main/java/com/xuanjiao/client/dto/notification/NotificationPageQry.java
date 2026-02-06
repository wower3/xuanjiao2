package com.xuanjiao.client.dto.notification;

import lombok.Data;

@Data
public class NotificationPageQry {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String notificationType;
    private Integer isRead;
    private String sourceType;
    private String keyword;
    private Long recipientId;
}
