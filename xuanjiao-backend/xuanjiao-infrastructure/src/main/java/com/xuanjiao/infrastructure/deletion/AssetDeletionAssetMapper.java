package com.xuanjiao.infrastructure.deletion;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 素材删除申请-素材关联Mapper
 */
@Mapper
public interface AssetDeletionAssetMapper extends BaseMapper<AssetDeletionAssetDO> {

    /**
     * 查询素材删除申请关联的所有素材（包含素材详细信息）
     * @param deletionApplicationId 删除申请ID
     * @return 素材删除申请-素材关联列表（包含素材详细信息）
     */
    @Select("SELECT ada.*, a.name as assetName, a.type as assetType, a.status as assetStatus, " +
            "a.file_path, a.thumbnail_path, a.file_size, a.description, a.publish_channel " +
            "FROM asset_deletion_asset ada " +
            "LEFT JOIN asset a ON a.id = ada.asset_id " +
            "WHERE ada.deletion_application_id = #{deletionApplicationId}")
    List<AssetDeletionAssetDO> findByDeletionApplicationIdWithAsset(@Param("deletionApplicationId") Long deletionApplicationId);
}
