package com.xuanjiao.client.notification;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量创建通知命令对象
 *
 * <p>封装批量创建通知所需的参数信息，用于同时给多个用户发送相同内容的通知。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class BatchCreateNotificationCmd {

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
     * 接收人ID列表
     */
    @NotEmpty(message = "recipients cannot be empty")
    private List<Long> recipientIds;
}
