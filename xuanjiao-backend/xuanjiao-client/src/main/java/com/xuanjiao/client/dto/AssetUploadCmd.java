package com.xuanjiao.client.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class AssetUploadCmd {
    @NotBlank(message = "素材名称不能为空")
    private String name;

    @NotBlank(message = "素材类型不能为空")
    private String type;

    private String copyright;
    private Long workflowId;

    // New fields for material entry
    private Long applicationId;
    private List<Long> tagIds;
    private String copyrightFilePath;
    private String copyrightText;
    private String description;
    private String publishChannel;
}
