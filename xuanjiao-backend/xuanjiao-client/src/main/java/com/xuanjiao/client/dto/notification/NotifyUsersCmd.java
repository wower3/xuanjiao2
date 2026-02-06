package com.xuanjiao.client.dto.notification;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 知会用户命令
 */
@Data
public class NotifyUsersCmd {

    /**
     * 审批实例ID
     */
    @NotNull(message = "审批实例ID不能为空")
    private Long instanceId;

    /**
     * 被知会的用户ID列表
     */
    @NotEmpty(message = "被知会用户不能为空")
    private List<Long> recipientIds;

    /**
     * 附加消息
     */
    private String message;
}
