package com.xuanjiao.domain.notification.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知实体
 *
 * <p>记录系统向用户发送的通知消息，包括审批通知、知会消息等。</p>
 * <p>支持已读状态跟踪，可标记单条或批量通知为已读。</p>
 * <p>通过sourceType和sourceId关联业务数据（如审批实例）。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class Notification {

    /**
     * 通知唯一标识
     *
     * <p>自增主键。</p>
     */
    private Long id;

    /**
     * 通知标题
     *
     * <p>简明扼要地说明通知内容。</p>
     */
    private String title;

    /**
     * 通知内容
     *
     * <p>详细的通知消息正文。</p>
     */
    private String content;

    /**
     * 通知类型
     *
     * <p>TASK-任务通知、SYSTEM-系统通知、MENTION-提及通知。</p>
     */
    private String notificationType;

    /**
     * 来源类型
     *
     * <p>APPROVAL-审批、USAGE-素材使用、DELETION-素材删除、MATERIAL-素材录入。</p>
     */
    private String sourceType;

    /**
     * 来源ID
     *
     * <p>关联业务记录ID（如审批实例ID）。</p>
     */
    private Long sourceId;

    /**
     * 发送人ID
     *
     * <p>关联sys_user表。</p>
     */
    private Long senderId;

    /**
     * 发送人名称
     */
    private String senderName;

    /**
     * 接收人ID
     *
     * <p>关联sys_user表。</p>
     */
    private Long recipientId;

    /**
     * 是否已读
     *
     * <p>0-未读、1-已读。</p>
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 通知状态
     *
     * <p>1-有效、0-无效。</p>
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
     *
     * <p>0-未删除、1-已删除。</p>
     */
    private Integer deleted;
}
