package com.xuanjiao.infrastructure.notification;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知会事项DO（包含工单信息，用于接收JOIN查询结果）
 */
@Data
public class NotificationWithWorkOrderDO {
    // ==================== 通知基础字段 ====================
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

    // ==================== 工单相关字段 ====================
    /**
     * 审批实例ID（工单ID）
     */
    private Long instanceId;

    /**
     * 工单状态
     */
    private String instanceStatus;

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
     * 获取工单ID（用于显示）
     */
    public String getDisplayWorkOrderId() {
        return instanceId != null ? "AP-" + instanceId : "";
    }

    /**
     * 获取显示标题（优先使用业务标题，其次使用通知标题）
     */
    public String getDisplayTitle() {
        if (businessTitle != null && !businessTitle.isEmpty()) {
            return businessTitle;
        }
        return title != null ? title : "";
    }
}
