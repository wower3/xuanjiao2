package com.xuanjiao.client.dto.notification;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量标记通知已读命令对象
 *
 * <p>用于批量将多个通知标记为已读状态。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class BatchMarkReadCmd {

    /**
     * 通知ID列表
     */
    @NotEmpty(message = "ids cannot be empty")
    private List<Long> ids;
}
