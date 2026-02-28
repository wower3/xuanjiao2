package com.xuanjiao.infrastructure.workflow;

import com.xuanjiao.infrastructure.dataobject.WorkflowStageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作流阶段数据访问接口
 *
 * <p>定义工作流阶段表的数据库操作方法，对应 XML Mapper 实现。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface WorkflowStageMapper {

    /**
     * 根据主键查询工作流阶段
     *
     * @param id 阶段ID
     * @return 工作流阶段数据对象
     */
    WorkflowStageDO selectById(@Param("id") Long id);

    /**
     * 动态条件查询工作流阶段列表
     *
     * @param query 查询条件
     * @return 工作流阶段数据对象列表
     */
    List<WorkflowStageDO> selectList(WorkflowStageQuery query);

    /**
     * 动态条件统计工作流阶段数量
     *
     * @param query 查询条件
     * @return 数量
     */
    Long selectCount(WorkflowStageQuery query);

    /**
     * 插入工作流阶段
     *
     * @param workflowStageDO 工作流阶段数据对象
     * @return 影响行数
     */
    int insert(WorkflowStageDO workflowStageDO);

    /**
     * 根据主键更新工作流阶段
     *
     * @param workflowStageDO 工作流阶段数据对象
     * @return 影响行数
     */
    int updateById(WorkflowStageDO workflowStageDO);

    /**
     * 根据主键删除工作流阶段（逻辑删除）
     *
     * @param id 阶段ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除工作流阶段（逻辑删除）
     *
     * @param query 查询条件
     * @return 影响行数
     */
    int delete(WorkflowStageQuery query);
}
