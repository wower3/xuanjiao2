package com.xuanjiao.client.approval;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 获取我的待办任务查询对象
 *
 * <p>用于查询当前用户待处理的审批任务列表，支持按业务类型筛选和分页。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalGetMyTasksQry {

    /**
     * 当前页码（从1开始，默认为1）
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页记录数（默认为10）
     */
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;

    /**
     * 业务类型筛选（可选）
     * <ul>
     *   <li>MATERIAL_ENTRY - 素材录入申请</li>
     *   <li>ASSET_USAGE - 素材使用申请</li>
     *   <li>ASSET_DELETION - 素材删除申请</li>
     * </ul>
     */
    private String businessType;
}
