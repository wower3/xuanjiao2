package com.xuanjiao.client.dto.material;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 更新素材申请单命令对象
 */
@Data
public class MaterialApplicationUpdateCmd {

    @NotNull(message = "申请单ID不能为空")
    private Long id;

    @NotBlank(message = "事项标题不能为空")
    private String title;

    private Long maintainerId;

    private Long deptId;

    private Integer guaranteeDeclaration;
}
