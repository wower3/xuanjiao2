package com.xuanjiao.infrastructure.notification;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知会记录DO（包含发起人和收件人的完整信息）
 */
@Data
public class NotificationRecordDO {
    // ==================== 通知基础信息 ====================
    private Long id;
    private String title;
    private String content;
    private String notificationType;
    private String sourceType;
    private Long sourceId;
    private Integer isRead;
    private LocalDateTime createTime;

    // ==================== 知会发起人信息 ====================
    private Long senderId;
    private String senderName;
    private String senderDeptName;

    // ==================== 知会收件人信息 ====================
    private Long recipientId;
    private String recipientName;
    private String recipientDeptName;

    // ==================== 辅助方法 ====================
    /**
     * 获取阅读状态文本
     */
    public String getReadStatusText() {
        return isRead != null && isRead == 1 ? "已读" : "未读";
    }
}
