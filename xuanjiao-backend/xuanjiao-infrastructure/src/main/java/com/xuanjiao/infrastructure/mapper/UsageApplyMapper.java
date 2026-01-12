package com.xuanjiao.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuanjiao.infrastructure.dataobject.UsageApplyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UsageApplyMapper extends BaseMapper<UsageApplyDO> {

    @Select("SELECT * FROM usage_apply WHERE asset_id = #{assetId} AND user_id = #{userId} AND status = #{status} AND deleted = 0 LIMIT 1")
    UsageApplyDO selectByAssetAndUser(@Param("assetId") Long assetId, @Param("userId") Long userId, @Param("status") String status);
}
