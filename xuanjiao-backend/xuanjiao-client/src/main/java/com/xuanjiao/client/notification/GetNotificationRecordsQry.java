package com.xuanjiao.client.notification;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 查询工单知会记录查询对象
 *
 * <p>用于查询指定工单（审批实例）的所有知会记录。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class GetNotificationRecordsQry {

    /**
     * 工单ID（审批实例ID）
     */
    @NotNull(message = "工单ID不能为空")
    private Long instanceId;
}
