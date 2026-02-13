package com.xuanjiao.infrastructure.usage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.dataobject.UsageApplyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 素材使用申请数据访问接口
 * <p>定义素材使用申请的数据库操作方法，对应SQL实现</p>
 * <p>支持多素材关联，通过usage_apply_asset中间表实现多对多关系</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.usage.entity.UsageApply
 */
@Mapper
public interface UsageApplyMapper {

    /**
     * 根据主键查询
     */
    UsageApplyDO selectById(@Param("id") Long id);

    /**
     * 自定义查询：根据素材ID和用户ID查询
     */
    UsageApplyDO selectByAssetAndUser(@Param("assetId") Long assetId, @Param("userId") Long userId, @Param("status") String status);

    /**
     * 动态条件查询列表
     */
    List<UsageApplyDO> selectList(UsageApplyQuery query);

    /**
     * 统计查询
     */
    Long selectCount(UsageApplyQuery query);

    /**
     * 分页查询
     */
    Page<UsageApplyDO> selectPage(Page<UsageApplyDO> page, @Param("query") UsageApplyQuery query);

    /**
     * 插入
     */
    int insert(UsageApplyDO usageApply);

    /**
     * 更新
     */
    int updateById(UsageApplyDO usageApply);

    /**
     * 删除（逻辑删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 动态条件查询列表（带申请人姓名，JOIN查询避免N+1问题）
     *
     * @param query 查询条件
     * @return 素材使用申请详情列表
     */
    List<UsageApplyWithDetailsDO> selectListWithDetails(UsageApplyQuery query);
}
