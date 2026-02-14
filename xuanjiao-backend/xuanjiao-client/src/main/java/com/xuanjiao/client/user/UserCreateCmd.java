package com.xuanjiao.client.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建用户命令对象
 *
 * <p>封装创建用户所需的参数信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UserCreateCmd {

    /**
     * 用户名（登录账号）
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 角色ID
     */
    private Long roleId;
}
