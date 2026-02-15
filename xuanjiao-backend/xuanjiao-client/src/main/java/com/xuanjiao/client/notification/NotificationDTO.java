package com.xuanjiao.client.notification;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知数据传输对象
 *
 * <p>用于在前后端之间传输通知信息，包括通知内容、发送人、
 * 接收人、已读状态等完整属性。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class NotificationDTO {

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
     * 通知类型（SYSTEM-系统通知、APPROVAL-审批通知等）
     */
    private String notificationType;

    /**
     * 来源类型（WORKFLOW-工作流、ASSET-素材等）
     */
    private String sourceType;

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
     * 是否已读（0-未读、1-已读）
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 通知类型文本（前端显示用，通过Service转换后填充）
     */
    private String notificationTypeText;

    /**
     * 来源类型文本（前端显示用，通过Service转换后填充）
     */
    private String sourceTypeText;

    // ========== 工单相关字段 ==========

    /**
     * 审批实例ID
     */
    private Long instanceId;

    /**
     * 审批实例状态
     */
    private String instanceStatus;

    /**
     * 流程定义ID
     */
    private Long workflowId;

    /**
     * 流程名称
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
     * 业务标题（素材名称/使用申请标题等）
     */
    private String businessTitle;

    /**
     * 显示用的工单ID
     */
    private String displayWorkOrderId;

    /**
     * 显示用的标题
     */
    private String displayTitle;

    /**
     * 状态文本（通过Service转换后填充）
     */
    private String statusText;
}
