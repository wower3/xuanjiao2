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
 * 工作流定义数据对象
 *
 * <p>映射数据库 workflow 表，用于 MyBatis 数据访问。</p>
 * <p>存储工作流定义信息，包括名称、版本、类型、绑定的角色等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("workflow")
public class WorkflowDO {

    /**
     * 工作流ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工作流名称
     */
    private String name;

    /**
     * 工作流描述
     */
    private String description;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 状态：1-启用、0-停用
     */
    private Integer status;

    /**
     * 绑定的角色ID（一个流程对应一个角色）
     */
    private Long boundRoleId;

    /**
     * 流程类型：ASSET_UPLOAD-素材录入审批、ASSET_USAGE-素材使用审批、ASSET_DELETION-素材删除审批
     */
    private String workflowType;

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
