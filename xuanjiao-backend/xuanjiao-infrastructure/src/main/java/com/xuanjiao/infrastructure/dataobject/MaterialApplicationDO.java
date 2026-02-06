package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材录入申请数据对象
 * <p>对应数据库表 material_application，存储素材录入申请的持久化数据</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.material.entity.MaterialApplication
 */
@Data
@TableName("material_application")
public class MaterialApplicationDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请标题 */
    private String title;

    /** 申请人ID */
    private Long applicantId;

    /** 维护人ID */
    private Long maintainerId;

    /** 部门ID */
    private Long deptId;

    /** 关联的工作流定义ID */
    private Long workflowId;

    /** 申请状态：DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝 */
    private String status;

    /** 版权保证声明：1-已声明、0-未声明 */
    private Integer guaranteeDeclaration;

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
