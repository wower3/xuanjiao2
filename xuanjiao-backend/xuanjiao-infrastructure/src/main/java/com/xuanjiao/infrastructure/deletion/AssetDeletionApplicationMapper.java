package com.xuanjiao.infrastructure.deletion;

import com.xuanjiao.infrastructure.dataobject.AssetDeletionApplicationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 素材删除申请Mapper
 */
@Mapper
public interface AssetDeletionApplicationMapper {

    // ==================== 基础CRUD方法 ====================

    /**
     * 根据ID查询
     */
    AssetDeletionApplicationDO selectById(@Param("id") Long id);

    /**
     * 根据查询条件查询单个记录
     */
    AssetDeletionApplicationDO selectOne(AssetDeletionApplicationQuery query);

    /**
     * 根据查询条件查询列表
     */
    List<AssetDeletionApplicationDO> selectList(AssetDeletionApplicationQuery query);

    /**
     * 根据查询条件统计数量
     */
    Long selectCount(AssetDeletionApplicationQuery query);

    /**
     * 分页查询
     */
    List<AssetDeletionApplicationDO> selectListWithPagination(@Param("offset") int offset,
                                                              @Param("limit") int limit,
                                                              @Param("query") AssetDeletionApplicationQuery query);

    /**
     * 插入记录
     */
    int insert(AssetDeletionApplicationDO assetDeletionApplicationDO);

    /**
     * 根据ID更新记录
     */
    int updateById(AssetDeletionApplicationDO assetDeletionApplicationDO);

    /**
     * 根据ID删除（软删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据查询条件删除（软删除）
     */
    int delete(AssetDeletionApplicationQuery query);
}
