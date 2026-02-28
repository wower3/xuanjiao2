package com.xuanjiao.client.dept;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取部门详情查询对象
 *
 * <p>用于根据部门ID获取部门的详细信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class DeptGetDetailQry {

    /**
     * 部门ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;
}
