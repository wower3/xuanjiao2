package com.xuanjiao.domain.user.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * <p>代表系统中的用户账号，包含登录信息和个人信息。</p>
 * <p>用户关联角色和部门，用于权限控制。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class User {

    /**
     * 用户唯一标识
     *
     * <p>自增主键，唯一标识一个用户。</p>
     */
    private Long id;

    /**
     * 用户名
     *
     * <p>登录账号，系统内唯一。</p>
     */
    private String username;

    /**
     * 登录密码
     *
     * <p>加密存储，不可明文。</p>
     */
    private String password;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 所属部门ID
     *
     * <p>关联sys_dept表。</p>
     */
    private Long deptId;

    /**
     * 角色ID
     *
     * <p>关联sys_role表。</p>
     */
    private Long roleId;

    /**
     * 用户状态
     *
     * <p>1-正常、0-禁用。</p>
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
