package com.xuanjiao.client.dto.approval;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 获取我的待办任务查询对象
 */
@Data
public class ApprovalGetMyTasksQry {

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;

    /**
     * 业务类型筛选（可选）
     * MATERIAL_ENTRY - 素材录入申请
     * ASSET_USAGE - 素材使用申请
     * ASSET_DELETION - 素材删除申请
     */
    private String businessType;
}
