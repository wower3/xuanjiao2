package com.xuanjiao.client.approval;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 获取我发起的审批查询对象
 *
 * <p>用于查询当前用户发起的所有审批申请，支持按业务类型、
 * 状态等条件筛选，管理员可查看所有用户的申请。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalGetMyAppliedQry {

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
     * 业务类型（MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除）
     */
    private String businessType;

    /**
     * 是否查询所有用户（仅管理员可用，默认为false）
     */
    private Boolean forAllUsers = false;

    /**
     * 申请人ID（用于按申请人筛选）
     */
    private Long applicantId;

    /**
     * 部门ID（用于按部门筛选）
     */
    private Long deptId;

    /**
     * 角色类型（用于按角色类型筛选）
     */
    private String roleType;

    /**
     * 审批状态（PENDING-待审批、APPROVED-已通过、REJECTED-已驳回、CANCELLED-已取消）
     */
    private String status;
}
