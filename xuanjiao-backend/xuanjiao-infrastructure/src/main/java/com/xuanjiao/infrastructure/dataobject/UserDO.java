package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户数据对象
 * <p>对应数据库表 sys_user，存储用户的持久化数据</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.user.entity.User
 */
@Data
@TableName("sys_user")
public class UserDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名，登录账号，唯一 */
    private String username;

    /** 登录密码，加密存储 */
    private String password;

    /** 用户真实姓名 */
    private String realName;

    /** 邮箱地址 */
    private String email;

    /** 联系电话 */
    private String phone;

    /** 所属部门ID */
    private Long deptId;

    /** 角色ID */
    @TableField("role_id")
    private Long roleId;

    /** 用户状态：1-正常、0-禁用 */
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
