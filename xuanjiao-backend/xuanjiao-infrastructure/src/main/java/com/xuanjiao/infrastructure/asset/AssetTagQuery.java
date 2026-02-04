package com.xuanjiao.infrastructure.asset;

import lombok.Data;

/**
 * AssetTag查询条件对象
 * 用于查询资产与标签的关联关系
 */
@Data
public class AssetTagQuery {
    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 标签ID
     */
    private Long tagId;
}
