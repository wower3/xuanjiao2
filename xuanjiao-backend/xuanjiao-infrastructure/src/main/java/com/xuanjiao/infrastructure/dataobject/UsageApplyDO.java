package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材使用申请数据对象
 * <p>对应数据库表 usage_apply，存储素材使用申请的持久化数据</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.usage.entity.UsageApply
 */
@Data
@TableName("usage_apply")
public class UsageApplyDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请人ID */
    private Long userId;

    /** 申请单标题 */
    private String title;

    /** 申请说明 */
    private String purpose;

    /** 使用范围 */
    private String scope;

    /** 关联的工作流定义ID */
    private Long workflowId;

    /** 申请状态：DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝 */
    private String status;

    /** 关联的审批实例ID */
    private Long approvalInstanceId;

    /** 附件路径 */
    private String attachmentPath;

    /** 是否二次创作：0-否、1-是 */
    private Integer isSecondaryCreation;

    /** 发布渠道 */
    private String publishChannel;

    /** 申请部门ID */
    private Long deptId;

    /** 是否草稿：0-已提交、1-草稿 */
    private Integer draft;

    /** 创建时间，自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标识 */
    @TableLogic
    private Integer deleted;
}
