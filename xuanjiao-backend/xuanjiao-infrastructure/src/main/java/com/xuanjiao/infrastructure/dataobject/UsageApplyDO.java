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
 * 素材使用申请数据对象
 *
 * <p>映射数据库 usage_apply 表，用于 MyBatis 数据访问。</p>
 * <p>存储素材使用申请信息，通过 usage_apply_asset 中间表关联多个素材。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("usage_apply")
public class UsageApplyDO {

    /**
     * 申请ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请人ID
     */
    private Long userId;

    /**
     * 申请单标题
     */
    private String title;

    /**
     * 申请说明
     */
    private String purpose;

    /**
     * 使用范围
     */
    private String scope;

    /**
     * 关联的工作流定义ID
     */
    private Long workflowId;

    /**
     * 申请状态：DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝
     */
    private String status;

    /**
     * 关联的审批实例ID
     */
    private Long approvalInstanceId;

    /**
     * 附件路径
     */
    private String attachmentPath;

    /**
     * 是否二次创作：0-否、1-是
     */
    private Integer isSecondaryCreation;

    /**
     * 发布渠道
     */
    private String publishChannel;

    /**
     * 申请部门ID
     */
    private Long deptId;

    /**
     * 是否草稿：0-已提交、1-草稿
     */
    private Integer draft;

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
