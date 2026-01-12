package com.xuanjiao.client.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UsageApplyCmd {
    @NotNull(message = "素材ID不能为空")
    private Long assetId;

    @NotBlank(message = "使用用途不能为空")
    private String purpose;

    @NotBlank(message = "使用范围不能为空")
    private String scope;

    private Long workflowId;
}
