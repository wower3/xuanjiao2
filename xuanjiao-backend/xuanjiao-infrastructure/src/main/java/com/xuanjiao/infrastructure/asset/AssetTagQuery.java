package com.xuanjiao.infrastructure.asset;

import lombok.Data;

/**
 * 素材-标签关联查询条件对象
 *
 * <p>用于查询素材与标签的关联关系。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class AssetTagQuery {

    /**
     * 素材ID
     */
    private Long assetId;

    /**
     * 标签ID
     */
    private Long tagId;
}
