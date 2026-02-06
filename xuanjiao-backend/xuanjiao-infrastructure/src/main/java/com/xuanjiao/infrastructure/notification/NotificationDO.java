package com.xuanjiao.infrastructure.notification;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知数据对象
 * <p>对应数据库表 sys_notification，存储系统通知的持久化数据</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.notification.entity.Notification
 */
@Data
@TableName("sys_notification")
public class NotificationDO {
    @TableId(type = IdType.AUTO)
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
    @TableLogic
    private Integer deleted;
}
