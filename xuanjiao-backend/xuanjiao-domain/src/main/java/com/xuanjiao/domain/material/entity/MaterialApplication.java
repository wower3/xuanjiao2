package com.xuanjiao.domain.material.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材录入申请实体
 *
 * <p>代表用户提交的素材录入申请，包含申请标题、申请人、关联工作流等信息。</p>
 * <p>素材录入申请需要经过审批流程才能将素材正式纳入系统。</p>
 * <p>一个申请可以包含多个素材（通过MaterialApplicationDetail关联）。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MaterialApplication {

    /**
     * 申请唯一标识
     *
     * <p>自增主键。</p>
     */
    private Long id;

    /**
     * 申请标题
     *
     * <p>简要说明本次录入的目的。</p>
     */
    private String title;

    /**
     * 申请人ID
     *
     * <p>关联sys_user表。</p>
     */
    private Long applicantId;

    /**
     * 申请人姓名（非数据库字段，用于显示）
     *
     * <p>通过JOIN查询获取。</p>
     */
    private String applicantName;

    /**
     * 维护人ID
     *
     * <p>关联sys_user表，负责该素材后续维护的用户。</p>
     */
    private Long maintainerId;

    /**
     * 维护人姓名（非数据库字段，用于显示）
     *
     * <p>通过JOIN查询获取。</p>
     */
    private String maintainerName;

    /**
     * 部门ID
     *
     * <p>关联sys_dept表，申请人所属部门。</p>
     */
    private Long deptId;

    /**
     * 部门名称（非数据库字段，用于显示）
     *
     * <p>通过JOIN查询获取。</p>
     */
    private String deptName;

    /**
     * 关联的工作流定义ID
     *
     * <p>用于审批该申请。</p>
     */
    private Long workflowId;

    /**
     * 申请状态
     *
     * <p>DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝。</p>
     */
    private String status;

    /**
     * 版权保证声明
     *
     * <p>1-已声明、0-未声明，申请人需声明素材版权情况。</p>
     */
    private Integer guaranteeDeclaration;

    /**
     * 逻辑删除标识
     *
     * <p>0-未删除、1-已删除。</p>
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
