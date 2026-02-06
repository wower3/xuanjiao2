package com.xuanjiao.infrastructure.notification;

import lombok.Data;

/**
 * 通知查询条件对象
 */
@Data
public class NotificationQuery {

    /** 主键ID */
    private Long id;

    /** 接收人ID */
    private Long recipientId;

    /** 通知类型 */
    private String notificationType;

    /** 是否已读 */
    private Integer isRead;

    /** 来源类型 */
    private String sourceType;

    /** 关键词搜索（工单ID、申请标题、知会人） */
    private String keyword;

    /** 排序字段 */
    private String orderByField;

    /** 排序方向（ASC/DESC） */
    private String orderByDirection;

    /** 分页偏移量 */
    private Integer offset;

    /** 分页大小 */
    private Integer limit;
}
