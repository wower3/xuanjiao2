package com.xuanjiao.client.dto.dept;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取部门详情查询对象
 */
@Data
public class DeptGetDetailQry {

    @NotNull(message = "ID不能为空")
    private Long id;
}
