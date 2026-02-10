package com.xuanjiao.client.dto.role;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除角色命令对象
 *
 * <p>用于删除指定的角色，删除前会检查是否存在关联用户。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class RoleDeleteCmd {

    /**
     * 角色ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;
}
