package com.xuanjiao.client.dto.material;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建素材申请单命令对象
 */
@Data
public class MaterialApplicationCreateCmd {

    @NotBlank(message = "事项标题不能为空")
    private String title;

    private Long maintainerId;

    private Long deptId;

    private Integer guaranteeDeclaration;
}
