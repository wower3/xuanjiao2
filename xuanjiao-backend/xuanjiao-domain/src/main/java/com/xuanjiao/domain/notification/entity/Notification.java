package com.xuanjiao.domain.notification.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知实体
 * <p>记录系统向用户发送的通知消息，包括审批通知、知会消息等</p>
 * <p>支持已读状态跟踪，可标记单条或批量通知为已读</p>
 * <p>通过sourceType和sourceId关联业务数据（如审批实例）</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.NotificationDO
 */
@Data
public class Notification {

    /** 通知唯一标识，自增主键 */
    private Long id;

    /** 通知标题，简明扼要地说明通知内容 */
    private String title;

    /** 通知内容，详细的通知消息正文 */
    private String content;

    /** 通知类型：TASK-任务通知、SYSTEM-系统通知、MENTION-提及通知 */
    private String notificationType;

    /** 来源类型：APPROVAL-审批、USAGE-素材使用、DELETION-素材删除、MATERIAL-素材录入 */
    private String sourceType;

    /** 来源ID，关联业务记录ID（如审批实例ID） */
    private Long sourceId;

    /** 发送人ID，关联sys_user表 */
    private Long senderId;

    /** 发送人名称 */
    private String senderName;

    /** 接收人ID，关联sys_user表 */
    private Long recipientId;

    /** 是否已读：0-未读、1-已读 */
    private Integer isRead;

    /** 阅读时间 */
    private LocalDateTime readTime;

    /** 通知状态：1-有效、0-无效 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除标识：0-未删除、1-已删除 */
    private Integer deleted;
}
