package com.xuanjiao.client.dto.notification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private String notificationType;
    private String sourceType;
    private Long sourceId;
    private Long senderId;
    private String senderName;
    private Long recipientId;
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;

    /**
     * 通知类型文本（前端显示用，通过Service转换后填充）
     */
    private String notificationTypeText;

    /**
     * 来源类型文本（前端显示用，通过Service转换后填充）
     */
    private String sourceTypeText;
}
