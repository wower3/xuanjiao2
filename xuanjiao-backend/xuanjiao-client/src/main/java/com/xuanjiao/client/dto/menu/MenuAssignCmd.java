package com.xuanjiao.client.dto.menu;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 菜单分配命令
 *
 * <p>用于为指定角色分配菜单权限。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MenuAssignCmd {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /**
     * 菜单ID列表
     */
    @NotEmpty(message = "菜单ID列表不能为空")
    private List<Long> menuIds;
}
