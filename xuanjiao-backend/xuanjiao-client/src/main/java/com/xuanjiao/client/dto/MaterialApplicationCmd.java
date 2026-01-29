package com.xuanjiao.client.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class MaterialApplicationCmd {
    @NotBlank(message = "事项标题不能为空")
    private String title;

    private Long maintainerId;

    private Long deptId;

    private Integer guaranteeDeclaration;
}
