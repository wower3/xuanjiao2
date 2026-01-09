package com.xuanjiao.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AssetMapper extends BaseMapper<AssetDO> {

    @Select("SELECT * FROM asset WHERE md5 = #{md5} LIMIT 1")
    AssetDO selectByMd5IncludeDeleted(@Param("md5") String md5);
}
