package com.xuanjiao.client.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 更新用户命令对象
 *
 * <p>封装更新用户所需的参数信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UserUpdateCmd {

    /**
     * 用户ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;

    /**
     * 用户名（登录账号）
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 真实姓名
     */
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
