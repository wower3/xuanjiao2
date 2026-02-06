package com.xuanjiao.domain.user.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 * <p>代表系统中的用户账号，包含登录信息和个人信息</p>
 * <p>用户关联角色和部门，用于权限控制</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.UserDO
 */
@Data
public class User {
    /** 用户唯一标识，自增主键 */
    private Long id;

    /** 用户名，登录账号 */
    private String username;

    /** 登录密码，加密存储 */
    private String password;

    /** 用户真实姓名 */
    private String realName;

    /** 邮箱地址 */
    private String email;

    /** 联系电话 */
    private String phone;

    /** 所属部门ID，关联sys_dept表 */
    private Long deptId;

    /** 角色ID，关联sys_role表 */
    private Long roleId;

    /** 用户状态：1-正常、0-禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
