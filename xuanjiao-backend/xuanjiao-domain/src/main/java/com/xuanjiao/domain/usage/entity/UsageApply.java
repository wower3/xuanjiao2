package com.xuanjiao.domain.usage.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 素材使用申请实体
 * <p>代表用户提交的使用素材申请，用于申请使用系统中的已有素材</p>
 * <p>申请通过审批后，用户可以下载和使用对应素材</p>
 * <p>支持一个申请包含多个素材（通过UsageApplyAsset中间表关联）</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.UsageApplyDO
 */
@Data
public class UsageApply {
    /** 申请唯一标识，自增主键 */
    private Long id;

    /** 申请人ID，关联sys_user表 */
    private Long userId;

    /** 申请单标题，简要说明使用目的 */
    private String title;

    /** 申请说明，详细描述素材的用途和使用场景 */
    private String purpose;

    /** 使用范围，说明素材的使用地域、时间等限制 */
    private String scope;

    /** 关联的工作流定义ID，用于审批该申请 */
    private Long workflowId;

    /** 申请状态：DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝 */
    private String status;

    /** 关联的审批实例ID，指向ApprovalInstance */
    private Long approvalInstanceId;

    /** 附件路径，相关证明文件的存储路径 */
    private String attachmentPath;

    /** 是否二次创作：0-否、1-是，声明素材是否经过二次创作 */
    private Integer isSecondaryCreation;

    /** 发布渠道，说明素材将发布到哪些渠道 */
    private String publishChannel;

    /** 申请部门ID，关联sys_dept表 */
    private Long deptId;

    /** 是否草稿：0-已提交、1-草稿，草稿状态的申请不进入审批流程 */
    private Integer draft;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 关联的素材列表（非数据库字段，用于业务逻辑） */
    private List<UsageApplyAsset> assets;
}
