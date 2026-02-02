package com.xuanjiao.client.dto.asset;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除标签命令对象
 */
@Data
public class TagDeleteCmd {

    @NotNull(message = "ID不能为空")
    private Long id;
}
