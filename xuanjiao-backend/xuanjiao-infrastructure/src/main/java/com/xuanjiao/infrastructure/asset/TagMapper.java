package com.xuanjiao.infrastructure.asset;

import com.xuanjiao.infrastructure.dataobject.TagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Tag Mapper - 标签表Mapper
 */
@Mapper
public interface TagMapper {

    /**
     * 根据ID查询标签
     */
    TagDO selectById(@Param("id") Long id);

    /**
     * 根据查询条件查询标签列表
     */
    List<TagDO> selectList(TagQuery query);

    /**
     * 根据查询条件统计数量
     */
    Long selectCount(TagQuery query);

    /**
     * 根据ID列表批量查询标签
     */
    List<TagDO> selectBatchIds(@Param("ids") List<Long> ids);

    /**
     * 插入标签
     */
    int insert(TagDO tagDO);

    /**
     * 更新标签
     */
    int updateById(TagDO tagDO);

    /**
     * 根据ID删除标签（软删除）
     */
    int deleteById(@Param("id") Long id);
}
