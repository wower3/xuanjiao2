package com.xuanjiao.infrastructure.asset;

import lombok.Data;

import java.util.List;

/**
 * 标签查询条件对象
 *
 * <p>用于动态构建标签查询条件，对应 TagMapper 使用。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
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
     * 排序方向（ASC/DESC）
     */
    private String orderByDirection;

    /**
     * ID列表（用于批量查询）
     */
    private List<Long> ids;
}
