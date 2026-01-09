package com.xuanjiao.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuanjiao.infrastructure.dataobject.UsageLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsageLogMapper extends BaseMapper<UsageLogDO> {
}
