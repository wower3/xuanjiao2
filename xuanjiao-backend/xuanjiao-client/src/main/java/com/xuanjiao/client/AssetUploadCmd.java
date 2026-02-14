package com.xuanjiao.client;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 素材上传命令
 *
 * <p>封装素材上传所需的参数信息，包括基本信息、版权信息、
 * 工作流关联和标签等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class AssetUploadCmd {

    /**
     * 素材名称
     */
    @NotBlank(message = "素材名称不能为空")
    private String name;

    /**
     * 素材类型（IMAGE-图片、VIDEO-视频、DOCUMENT-文档）
     */
    @NotBlank(message = "素材类型不能为空")
    private String type;

    /**
     * 版权信息
     */
    private String copyright;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 关联的素材录入申请ID
     */
    private Long applicationId;

    /**
     * 关联的标签ID列表
     */
    private List<Long> tagIds;

    /**
     * 版权文件路径
     */
    private String copyrightFilePath;

    /**
     * 版权说明文字
     */
    private String copyrightText;

    /**
     * 素材描述
     */
    private String description;

    /**
     * 发布渠道
     */
    private String publishChannel;
}
