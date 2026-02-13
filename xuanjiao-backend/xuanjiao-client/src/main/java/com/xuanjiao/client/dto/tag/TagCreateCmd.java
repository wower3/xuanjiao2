package com.xuanjiao.client.dto.tag;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建标签命令
 *
 * <p>用于创建新的素材标签。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class TagCreateCmd {

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    private String name;

    /**
     * 标签分类（IMAGE-图片，VIDEO-视频）
     */
    private String category;
}
