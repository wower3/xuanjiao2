package com.xuanjiao.infrastructure.deletion;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuanjiao.infrastructure.dataobject.AssetDeletionApplicationDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 素材删除申请Mapper
 */
@Mapper
public interface AssetDeletionApplicationMapper extends BaseMapper<AssetDeletionApplicationDO> {
}
