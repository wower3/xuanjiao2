package com.xuanjiao.infrastructure.usage;

import com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 素材使用申请-素材关联数据访问接口
 * <p>定义使用申请与素材关联的数据库操作方法，对应SQL实现</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.usage.entity.UsageApplyAsset
 */
@Mapper
public interface UsageApplyAssetMapper {

    // ==================== 基础CRUD方法 ====================

    /**
     * 根据查询条件查询关联列表
     */
    List<UsageApplyAssetDO> selectList(UsageApplyAssetQuery query);

    /**
     * 根据查询条件统计数量
     */
    Long selectCount(UsageApplyAssetQuery query);

    /**
     * 插入关联
     */
    int insert(UsageApplyAssetDO usageApplyAssetDO);

    /**
     * 更新关联
     */
    int updateById(UsageApplyAssetDO usageApplyAssetDO);

    /**
     * 根据ID删除关联（硬删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据申请单ID删除所有关联（硬删除）
     */
    int deleteByUsageApplyId(@Param("usageApplyId") Long usageApplyId);

    /**
     * 根据查询条件删除关联（硬删除）
     */
    int delete(UsageApplyAssetQuery query);

    // ==================== 自定义查询方法（保留原有功能）====================

    /**
     * 根据申请单ID查询关联的素材（包含素材详情）
     */
    List<UsageApplyAssetDO> findByUsageApplyIdWithAsset(@Param("usageApplyId") Long usageApplyId);

    /**
     * 根据素材ID查询所有关联的申请单
     */
    List<UsageApplyAssetDO> findByAssetId(@Param("assetId") Long assetId);

    /**
     * 根据申请单ID和素材ID查询
     */
    UsageApplyAssetDO findByUsageApplyIdAndAssetId(@Param("usageApplyId") Long usageApplyId,
                                                     @Param("assetId") Long assetId);
}
