package com.xuanjiao.infrastructure.asset;

import lombok.Data;

/**
 * Tag查询条件对象
 * 用于查询标签信息
 */
@Data
public class TagQuery {
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
     * 删除标记
     */
    private Integer deleted;

    /**
     * 排序字段
     */
    private String orderByField;

    /**
     * 排序方向 (ASC/DESC)
     */
    private String orderByDirection;

    /**
     * ID列表（用于批量查询）
     */
    private java.util.List<Long> ids;
}
