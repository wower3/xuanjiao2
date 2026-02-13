package com.xuanjiao.infrastructure.asset;

import com.xuanjiao.infrastructure.dataobject.AssetTagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 素材-标签关联数据访问接口
 *
 * <p>定义素材标签关联的数据库操作方法，对应 XML Mapper 实现。</p>
 * <p>注意：此表为中间表，只有 asset_id 和 tag_id 两个字段，无 id 字段和 deleted 字段。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface AssetTagMapper {

    /**
     * 根据查询条件查询素材标签关联列表
     *
     * @param query 查询条件
     * @return 素材标签关联列表
     */
    List<AssetTagDO> selectList(AssetTagQuery query);

    /**
     * 根据查询条件统计数量
     *
     * @param query 查询条件
     * @return 数量
     */
    Long selectCount(AssetTagQuery query);

    /**
     * 插入素材标签关联
     *
     * @param assetTagDO 素材标签关联数据对象
     * @return 影响行数
     */
    int insert(AssetTagDO assetTagDO);

    /**
     * 根据查询条件删除素材标签关联（硬删除）
     *
     * @param query 查询条件
     * @return 影响行数
     */
    int delete(AssetTagQuery query);

    /**
     * 批量查询多个素材的标签关联（优化N+1问题）
     *
     * @param assetIds 素材ID列表
     * @return 素材标签关联列表
     */
    List<AssetTagDO> selectByAssetIds(@Param("assetIds") List<Long> assetIds);
}
