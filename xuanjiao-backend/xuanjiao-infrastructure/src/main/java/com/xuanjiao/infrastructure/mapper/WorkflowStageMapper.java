package com.xuanjiao.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuanjiao.infrastructure.dataobject.WorkflowStageDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkflowStageMapper extends BaseMapper<WorkflowStageDO> {
}
