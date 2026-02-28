package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 阶段审批人数据对象
 *
 * <p>映射数据库 stage_approver 表，用于 MyBatis 数据访问。</p>
 * <p>存储工作流阶段的审批人配置，支持用户、角色、部门等不同类型的审批人。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("stage_approver")
public class StageApproverDO {

    /**
     * 审批人配置ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的工作流阶段ID
     */
    private Long stageId;

    /**
     * 审批人类型：USER-用户、ROLE-角色、DEPT-部门
     */
    private String approverType;

    /**
     * 审批人ID，根据approverType关联不同的表
     */
    private Long approverId;

    /**
     * 是否校验二级部门（仅当approver_type=ROLE时有效）：0-否、1-是
     */
    private Integer checkSecondaryDept;

    /**
     * 关联的子流程ID（如果该审批人是子流程入口）
     */
    private Long subWorkflowId;
}
