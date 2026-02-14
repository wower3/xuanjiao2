package com.xuanjiao.client.notification;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量删除通知命令对象
 *
 * <p>用于批量删除多个通知。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class BatchDeleteNotificationCmd {

    /**
     * 通知ID列表
     */
    @NotEmpty(message = "ids cannot be empty")
    private List<Long> ids;
}
