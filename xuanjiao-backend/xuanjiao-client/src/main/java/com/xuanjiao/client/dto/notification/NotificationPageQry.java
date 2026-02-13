package com.xuanjiao.client.dto.notification;

import com.xuanjiao.client.dto.common.BasePageQry;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知分页查询对象
 *
 * <p>用于分页查询通知列表，支持按通知类型、已读状态、来源类型和关键词筛选。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationPageQry extends BasePageQry {

    /**
     * 通知类型（SYSTEM-系统通知、APPROVAL-审批通知等）
     */
    private String notificationType;

    /**
     * 是否已读（0-未读、1-已读）
     */
    private Integer isRead;

    /**
     * 来源类型（WORKFLOW-工作流、ASSET-素材等）
     */
    private String sourceType;

    /**
     * 搜索关键词（匹配标题或内容）
     */
    private String keyword;

    /**
     * 接收人ID
     */
    private Long recipientId;
}
