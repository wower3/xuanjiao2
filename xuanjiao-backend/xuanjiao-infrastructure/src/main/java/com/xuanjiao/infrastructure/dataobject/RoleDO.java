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
 * 角色数据对象
 *
 * <p>映射数据库 sys_role 表，用于 MyBatis 数据访问。</p>
 * <p>存储系统角色信息，用于权限控制和用户分组。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("sys_role")
public class RoleDO {

    /**
     * 角色ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 部门范围：ALL-全部、SELF-本部门、SELF_AND_SUB-本部门及子部门
     */
    private String deptScope;

    /**
     * 角色类型：ADMIN-管理员、APPROVER-审批人、USER-普通用户
     */
    private String roleType;

    /**
     * 角色状态：1-正常、0-停用
     */
    private Integer status;

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
