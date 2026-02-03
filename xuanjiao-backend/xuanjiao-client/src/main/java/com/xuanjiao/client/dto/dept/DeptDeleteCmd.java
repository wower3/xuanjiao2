package com.xuanjiao.client.dto.dept;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除部门命令对象
 */
@Data
public class DeptDeleteCmd {

    @NotNull(message = "ID不能为空")
    private Long id;
}
