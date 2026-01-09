package com.xuanjiao.client.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class AssetUploadCmd {
    @NotBlank(message = "素材名称不能为空")
    private String name;

    @NotBlank(message = "素材类型不能为空")
    private String type;

    private String copyright;
    private Long workflowId;
}
