package com.xuanjiao.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuanjiao.infrastructure.dataobject.ApprovalProgressDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ApprovalProgressMapper extends BaseMapper<ApprovalProgressDO> {

    /**
     * 根据实例ID查询进度列表
     */
    @Select("SELECT * FROM approval_progress WHERE instance_id = #{instanceId} ORDER BY stage_order ASC")
    List<ApprovalProgressDO> selectByInstanceId(@Param("instanceId") Long instanceId);

    /**
     * 根据父实例ID查询所有子流程进度
     */
    @Select("SELECT * FROM approval_progress WHERE parent_instance_id = #{parentInstanceId} ORDER BY instance_id, stage_order ASC")
    List<ApprovalProgressDO> selectByParentInstanceId(@Param("parentInstanceId") Long parentInstanceId);
}
