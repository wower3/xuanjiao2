package com.xuanjiao.infrastructure.material;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.dataobject.MaterialApplicationDO;
import com.xuanjiao.infrastructure.dataobject.MaterialApplicationWithDetailsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 素材录入申请数据访问接口
 * <p>定义素材录入申请的数据库操作方法，对应SQL实现</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.material.entity.MaterialApplication
 */
@Mapper
public interface MaterialApplicationMapper {

    /**
     * 根据主键查询素材申请
     */
    MaterialApplicationDO selectById(@Param("id") Long id);

    /**
     * 条件统计素材申请数量
     */
    long selectCount(MaterialApplicationQuery query);

    /**
     * 分页查询素材申请
     *
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<MaterialApplicationDO> selectPage(Page<MaterialApplicationDO> page, @Param("query") MaterialApplicationQuery query);

    /**
     * 插入素材申请
     */
    int insert(MaterialApplicationDO application);

    /**
     * 根据主键更新素材申请
     */
    int updateById(MaterialApplicationDO application);

    /**
     * 根据主键删除素材申请（逻辑删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 分页查询素材申请列表（JOIN 用户和部门，避免 N+1）
     *
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<MaterialApplicationWithDetailsDO> selectPageWithDetails(Page<MaterialApplicationWithDetailsDO> page, @Param("query") MaterialApplicationQuery query);
}
