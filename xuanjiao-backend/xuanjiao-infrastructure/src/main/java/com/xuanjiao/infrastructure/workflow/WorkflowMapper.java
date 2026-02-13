package com.xuanjiao.infrastructure.workflow;

import com.xuanjiao.infrastructure.dataobject.WorkflowDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工作流数据访问接口
 *
 * <p>定义工作流表的数据库操作方法，对应 XML Mapper 实现。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface WorkflowMapper {

    /**
     * 根据主键查询工作流
     *
     * @param id 工作流ID
     * @return 工作流数据对象
     */
    WorkflowDO selectById(@Param("id") Long id);

    /**
     * 动态条件查询工作流列表
     *
     * @param query 查询条件
     * @return 工作流数据对象列表
     */
    List<WorkflowDO> selectList(WorkflowQuery query);

    /**
     * 查询工作流列表（带角色名称，通过JOIN）
     *
     * @param query 查询条件
     * @return 工作流数据对象列表，包含角色名称
     */
    List<WorkflowDO> selectListWithRoleName(WorkflowQuery query);

    /**
     * 动态条件统计工作流数量
     *
     * @param query 查询条件
     * @return 数量
     */
    Long selectCount(WorkflowQuery query);

    /**
     * 插入工作流
     *
     * @param workflowDO 工作流数据对象
     * @return 影响行数
     */
    int insert(WorkflowDO workflowDO);

    /**
     * 根据主键更新工作流
     *
     * @param workflowDO 工作流数据对象
     * @return 影响行数
     */
    int updateById(WorkflowDO workflowDO);

    /**
     * 根据主键删除工作流（逻辑删除）
     *
     * @param id 工作流ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}
