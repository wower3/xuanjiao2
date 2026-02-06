package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色数据对象
 * <p>对应数据库表 sys_role，存储角色的持久化数据</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.role.entity.Role
 */
@Data
@TableName("sys_role")
public class RoleDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色名称 */
    private String name;

    /** 角色描述 */
    private String description;

    /** 部门范围：ALL-全部、SELF-本部门、SELF_AND_SUB-本部门及子部门 */
    private String deptScope;

    /** 角色类型：ADMIN-管理员、APPROVER-审批人、USER-普通用户 */
    private String roleType;

    /** 角色状态：1-正常、0-停用 */
    private Integer status;

    /** 创建时间，自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标识：0-未删除、1-已删除 */
    @TableLogic
    private Integer deleted;
}
