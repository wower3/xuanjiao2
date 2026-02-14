package com.xuanjiao.client.user;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除用户命令对象
 *
 * <p>用于删除指定的用户。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UserDeleteCmd {

    /**
     * 用户ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;
}
