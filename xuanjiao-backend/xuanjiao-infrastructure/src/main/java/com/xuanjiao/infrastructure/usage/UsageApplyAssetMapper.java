package com.xuanjiao.infrastructure.usage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 素材使用申请-素材关联Mapper
 */
@Mapper
public interface UsageApplyAssetMapper extends BaseMapper<UsageApplyAssetDO> {

    /**
     * 根据申请单ID查询关联的素材（包含素材详情）
     */
    @Select("SELECT ua.*, a.name as asset_name, a.type as asset_type, a.file_path as asset_file_path, " +
            "a.thumbnail_path as asset_thumbnail_path, a.status as asset_status " +
            "FROM usage_apply_asset ua " +
            "LEFT JOIN asset a ON ua.asset_id = a.id " +
            "WHERE ua.usage_apply_id = #{usageApplyId}")
    List<UsageApplyAssetDO> findByUsageApplyIdWithAsset(@Param("usageApplyId") Long usageApplyId);

    /**
     * 根据素材ID查询所有关联的申请单
     */
    @Select("SELECT * FROM usage_apply_asset WHERE asset_id = #{assetId}")
    List<UsageApplyAssetDO> findByAssetId(@Param("assetId") Long assetId);

    /**
     * 根据申请单ID和素材ID查询
     */
    @Select("SELECT * FROM usage_apply_asset WHERE usage_apply_id = #{usageApplyId} AND asset_id = #{assetId}")
    UsageApplyAssetDO findByUsageApplyIdAndAssetId(@Param("usageApplyId") Long usageApplyId,
                                                     @Param("assetId") Long assetId);

    /**
     * 根据申请单ID删除所有关联
     */
    @Delete("DELETE FROM usage_apply_asset WHERE usage_apply_id = #{usageApplyId}")
    int deleteByUsageApplyId(@Param("usageApplyId") Long usageApplyId);
}
