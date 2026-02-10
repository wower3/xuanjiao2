package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审批进度数据对象
 *
 * <p>映射数据库 approval_progress 表，用于 MyBatis 数据访问。</p>
 * <p>存储审批进度信息，用于前端展示审批流程图。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("approval_progress")
public class ApprovalProgressDO {

    /**
     * 进度ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 审批实例ID
     */
    private Long instanceId;

    /**
     * 阶段ID
     */
    private Long stageId;

    /**
     * 阶段名称
     */
    private String stageName;

    /**
     * 阶段顺序
     */
    private Integer stageOrder;

    /**
     * 状态：PENDING-待审批、APPROVED-已通过、REJECTED-已驳回、SKIPPED-已跳过
     */
    private String status;

    /**
     * 审批人列表，JSON格式：[{id, name, status, approveTime}]
     */
    private String approvers;

    /**
     * 是否是子流程：0-否、1-是
     */
    private Integer isSubWorkflow;

    /**
     * 父实例ID（用于子流程）
     */
    private Long parentInstanceId;

    /**
     * 父任务ID（用于子流程，记录是哪个任务触发的子流程）
     */
    private Long parentTaskId;

    /**
     * 审批通过时间
     */
    private LocalDateTime approveTime;

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
}
