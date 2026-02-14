package com.xuanjiao.client.asset;

import lombok.Data;

/**
 * 根据分类获取标签查询对象
 *
 * <p>用于按标签分类筛选查询标签列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class TagGetListByCategoryQry {

    /**
     * 标签分类
     */
    private String category;
}
