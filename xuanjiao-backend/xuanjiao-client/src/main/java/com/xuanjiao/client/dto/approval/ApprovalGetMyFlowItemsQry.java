package com.xuanjiao.client.dto.approval;

import lombok.Data;

/**
 * 流经事项查询DTO
 */
@Data
public class ApprovalGetMyFlowItemsQry {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 业务类型筛选: MATERIAL_ENTRY, ASSET_USAGE, ASSET_DELETION
     */
    private String businessType;

    /**
     * 审批状态筛选: PENDING, APPROVED, REJECTED, CANCELLED
     */
    private String status;
}
