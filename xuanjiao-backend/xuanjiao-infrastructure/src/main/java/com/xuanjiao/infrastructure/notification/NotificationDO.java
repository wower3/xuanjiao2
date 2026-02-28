package com.xuanjiao.infrastructure.notification;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知数据对象
 *
 * <p>映射数据库 sys_notification 表，用于 MyBatis 数据访问。</p>
 * <p>存储系统通知信息，包括知会消息、流程流转通知等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("sys_notification")
public class NotificationDO {

    /**
     * 通知ID（主键）
     */
    @TableId(type = IdType.AUTO)
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
     * 通知类型：WORKFLOW_FLOW-流程流转、MENTION-知会、SYSTEM-系统通知
     */
    private String notificationType;

    /**
     * 来源类型：MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除
     */
    private String sourceType;

    /**
     * 来源ID（关联的业务ID）
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
     * 是否已读：0-未读、1-已读
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 状态：1-正常、0-删除
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
     * 逻辑删除标识：0-未删除、1-已删除
     */
    @TableLogic
    private Integer deleted;
}
