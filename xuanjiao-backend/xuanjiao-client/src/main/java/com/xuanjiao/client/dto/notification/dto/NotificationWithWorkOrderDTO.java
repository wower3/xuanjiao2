package com.xuanjiao.client.dto.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知会事项数据传输对象
 *
 * <p>用于在知会列表中展示完整的知会事项，包含通知信息和关联的工单信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class NotificationWithWorkOrderDTO {

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
     * 通知类型文本
     */
    private String notificationTypeText;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 来源类型文本
     */
    private String sourceTypeText;

    /**
     * 来源ID
     */
    private Long sourceId;

    /**
     * 发送人ID
     */
    private Long senderId;

    /**
     * 发送人姓名
     */
    private String senderName;

    /**
     * 接收人ID
     */
    private Long recipientId;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 审批实例ID（工单ID）
     */
    private Long instanceId;

    /**
     * 工单显示ID
     */
    private String displayWorkOrderId;

    /**
     * 工单状态
     */
    private String status;

    /**
     * 工单状态文本
     */
    private String statusText;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 业务标题（申请标题）
     */
    private String businessTitle;

    /**
     * 显示标题
     */
    private String displayTitle;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
