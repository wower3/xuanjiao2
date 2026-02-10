package com.xuanjiao.infrastructure.notification;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知会事项数据对象
 *
 * <p>用于接收 JOIN 查询结果，包含通知信息和关联的工单信息。</p>
 * <p>用于在知会列表中展示完整的知会事项。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class NotificationWithWorkOrderDO {

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
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识
     */
    private Integer deleted;

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
     * 获取工单显示ID
     *
     * @return 工单显示ID（格式：AP-{instanceId}）
     */
    public String getDisplayWorkOrderId() {
        return instanceId != null ? "AP-" + instanceId : "";
    }

    /**
     * 获取显示标题
     *
     * <p>优先使用业务标题，其次使用通知标题。</p>
     *
     * @return 显示标题
     */
    public String getDisplayTitle() {
        if (businessTitle != null && !businessTitle.isEmpty()) {
            return businessTitle;
        }
        return title != null ? title : "";
    }
}
