package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材录入申请数据对象
 *
 * <p>映射数据库 material_application 表，用于 MyBatis 数据访问。</p>
 * <p>存储素材录入申请信息，包括申请标题、申请人、状态等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("material_application")
public class MaterialApplicationDO {

    /**
     * 申请ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请标题
     */
    private String title;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 维护人ID
     */
    private Long maintainerId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 关联的工作流定义ID
     */
    private Long workflowId;

    /**
     * 申请状态：DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝
     */
    private String status;

    /**
     * 版权保证声明：1-已声明、0-未声明
     */
    private Integer guaranteeDeclaration;

    /**
     * 创建时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识：0-未删除、1-已删除
     */
    @TableLogic
    private Integer deleted;
}
