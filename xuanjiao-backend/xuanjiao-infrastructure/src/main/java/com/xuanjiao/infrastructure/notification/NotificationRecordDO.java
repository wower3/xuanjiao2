package com.xuanjiao.infrastructure.notification;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知会记录数据对象
 *
 * <p>用于接收 JOIN 查询结果，包含发起人和收件人的完整信息。</p>
 * <p>用于展示工单的知会记录详情。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class NotificationRecordDO {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型
     */
    private String notificationType;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 来源ID
     */
    private Long sourceId;

    /**
     * 是否已读：0-未读、1-已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 知会发起人ID
     */
    private Long senderId;

    /**
     * 知会发起人姓名
     */
    private String senderName;

    /**
     * 知会发起人部门名称
     */
    private String senderDeptName;

    /**
     * 知会收件人ID
     */
    private Long recipientId;

    /**
     * 知会收件人姓名
     */
    private String recipientName;

    /**
     * 知会收件人部门名称
     */
    private String recipientDeptName;

    /**
     * 获取阅读状态文本
     *
     * @return 阅读状态文本（已读/未读）
     */
    public String getReadStatusText() {
        return isRead != null && isRead == 1 ? "已读" : "未读";
    }
}
