package com.xuanjiao.infrastructure.asset;

import com.xuanjiao.infrastructure.dataobject.AssetTagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AssetTag Mapper - 资产标签关联表Mapper
 * 注意：此表为中间表，只有asset_id和tag_id两个字段，无id字段和deleted字段
 */
@Mapper
public interface AssetTagMapper {

    /**
     * 根据查询条件查询资产标签关联列表
     */
    List<AssetTagDO> selectList(AssetTagQuery query);

    /**
     * 根据查询条件统计数量
     */
    Long selectCount(AssetTagQuery query);

    /**
     * 插入资产标签关联
     */
    int insert(AssetTagDO assetTagDO);

    /**
     * 根据查询条件删除资产标签关联（硬删除）
     */
    int delete(AssetTagQuery query);
}
