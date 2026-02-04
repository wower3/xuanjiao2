package com.xuanjiao.infrastructure.deletion;

import com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 素材删除申请-素材关联Mapper
 */
@Mapper
public interface AssetDeletionAssetMapper {

    // ==================== 基础CRUD方法 ====================

    /**
     * 根据ID查询关联
     */
    AssetDeletionAssetDO selectById(@Param("id") Long id);

    /**
     * 根据查询条件查询关联列表
     */
    List<AssetDeletionAssetDO> selectList(AssetDeletionAssetQuery query);

    /**
     * 根据查询条件统计数量
     */
    Long selectCount(AssetDeletionAssetQuery query);

    /**
     * 插入关联
     */
    int insert(AssetDeletionAssetDO assetDeletionAssetDO);

    /**
     * 更新关联
     */
    int updateById(AssetDeletionAssetDO assetDeletionAssetDO);

    /**
     * 根据ID删除关联（硬删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据查询条件删除关联（硬删除）
     */
    int delete(AssetDeletionAssetQuery query);

    // ==================== 自定义查询方法（保留原有功能）====================

    /**
     * 根据删除申请ID查询关联的素材（包含素材详情）
     */
    List<AssetDeletionAssetDO> findByDeletionApplicationIdWithAsset(@Param("deletionApplicationId") Long deletionApplicationId);
}
