package com.xuanjiao.client.asset;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 标签数据传输对象
 *
 * <p>用于在前后端之间传输标签信息，标签用于对素材进行分类标记。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class TagDTO {

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签分类
     */
    private String category;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
