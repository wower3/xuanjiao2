package com.xuanjiao.infrastructure.material;

import com.xuanjiao.infrastructure.dataobject.MaterialApplicationDO;
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
     * 条件查询素材申请列表
     * 使用MaterialApplicationQuery对象封装查询条件，支持动态条件组合
     */
    List<MaterialApplicationDO> selectList(MaterialApplicationQuery query);

    /**
     * 条件统计素材申请数量
     */
    long selectCount(MaterialApplicationQuery query);

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
     * 条件查询素材申请列表（带申请人，维护人、部门名称，JOIN查询避免N+1问题）
     *
     * <p>一次性获取素材申请及关联的申请人、维护人、部门信息。</p>
     *
     * @param query 查询条件
     * @return 素材申请详情列表
     */
    List<MaterialApplicationWithDetailsDO> selectListWithDetails(MaterialApplicationQuery query);
}
