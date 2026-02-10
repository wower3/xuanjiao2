package com.xuanjiao.infrastructure.asset;

import com.xuanjiao.infrastructure.dataobject.TagDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签数据访问接口
 *
 * <p>定义标签表的数据库操作方法，对应 XML Mapper 实现。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface TagMapper {

    /**
     * 根据ID查询标签
     *
     * @param id 标签ID
     * @return 标签数据对象
     */
    TagDO selectById(@Param("id") Long id);

    /**
     * 根据查询条件查询标签列表
     *
     * @param query 查询条件
     * @return 标签数据对象列表
     */
    List<TagDO> selectList(TagQuery query);

    /**
     * 根据查询条件统计数量
     *
     * @param query 查询条件
     * @return 数量
     */
    Long selectCount(TagQuery query);

    /**
     * 根据ID列表批量查询标签
     *
     * @param ids ID列表
     * @return 标签数据对象列表
     */
    List<TagDO> selectBatchIds(@Param("ids") List<Long> ids);

    /**
     * 插入标签
     *
     * @param tagDO 标签数据对象
     * @return 影响行数
     */
    int insert(TagDO tagDO);

    /**
     * 更新标签
     *
     * @param tagDO 标签数据对象
     * @return 影响行数
     */
    int updateById(TagDO tagDO);

    /**
     * 根据ID删除标签（软删除）
     *
     * @param id 标签ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}
