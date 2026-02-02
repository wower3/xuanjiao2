package com.xuanjiao.client.dto.material;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除素材申请单命令对象
 */
@Data
public class MaterialApplicationDeleteCmd {

    @NotNull(message = "申请单ID不能为空")
    private Long id;
}
