package com.xuanjiao.domain.material.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材录入申请实体
 * <p>代表用户提交的素材录入申请，包含申请标题、申请人、关联工作流等信息</p>
 * <p>素材录入申请需要经过审批流程才能将素材正式纳入系统</p>
 * <p>一个申请可以包含多个素材（通过MaterialApplicationDetail关联）</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.MaterialApplicationDO
 */
@Data
public class MaterialApplication {
    /** 申请唯一标识，自增主键 */
    private Long id;

    /** 申请标题，简要说明本次录入的目的 */
    private String title;

    /** 申请人ID，关联sys_user表 */
    private Long applicantId;

    /** 维护人ID，关联sys_user表，负责该素材后续维护的用户 */
    private Long maintainerId;

    /** 部门ID，关联sys_dept表，申请人所属部门 */
    private Long deptId;

    /** 关联的工作流定义ID，用于审批该申请 */
    private Long workflowId;

    /** 申请状态：DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝 */
    private String status;

    /** 版权保证声明：1-已声明、0-未声明，申请人需声明素材版权情况 */
    private Integer guaranteeDeclaration;

    /** 逻辑删除标识：0-未删除、1-已删除 */
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
