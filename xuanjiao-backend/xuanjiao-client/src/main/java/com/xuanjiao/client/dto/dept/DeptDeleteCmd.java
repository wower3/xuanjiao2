package com.xuanjiao.client.dto.dept;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除部门命令对象
 *
 * <p>用于删除指定的部门，删除前会检查是否存在子部门或关联用户。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class DeptDeleteCmd {

    /**
     * 部门ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;
}
