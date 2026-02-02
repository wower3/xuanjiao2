package com.xuanjiao.client.dto.user;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除用户命令对象
 */
@Data
public class UserDeleteCmd {

    @NotNull(message = "ID不能为空")
    private Long id;
}
