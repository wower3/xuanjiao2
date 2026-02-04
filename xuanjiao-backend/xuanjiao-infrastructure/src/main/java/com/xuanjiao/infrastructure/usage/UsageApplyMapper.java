package com.xuanjiao.infrastructure.usage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.dataobject.UsageApplyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
}
