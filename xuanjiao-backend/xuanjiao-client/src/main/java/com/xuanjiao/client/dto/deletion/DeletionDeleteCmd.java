package com.xuanjiao.client.dto.deletion;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除删除申请命令对象
 */
@Data
public class DeletionDeleteCmd {

    @NotNull(message = "申请单ID不能为空")
    private Long id;
}
