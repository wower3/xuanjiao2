package com.xuanjiao.domain.notification.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知实体
 */
@Data
public class Notification {

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
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
