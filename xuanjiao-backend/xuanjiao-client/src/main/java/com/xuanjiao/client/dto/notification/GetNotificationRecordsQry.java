package com.xuanjiao.client.dto.notification;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 查询工单知会记录Qry
 */
@Data
public class GetNotificationRecordsQry {

    /**
     * 工单ID（审批实例ID）
     */
    @NotNull(message = "工单ID不能为空")
    private Long instanceId;
}
