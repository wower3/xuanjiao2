package com.xuanjiao.client.dto.menu;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取菜单详情查询对象
 *
 * <p>用于根据菜单ID获取菜单的详细信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MenuGetDetailQry {

    /**
     * 菜单ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;
}
