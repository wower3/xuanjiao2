package com.xuanjiao.infrastructure.workflow;

import com.xuanjiao.infrastructure.dataobject.StageApproverDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 阶段审批人数据访问接口
 *
 * <p>定义阶段审批人的数据库操作方法，对应 XML Mapper 实现。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface StageApproverMapper {

    /**
     * 根据主键查询阶段审批人
     *
     * @param id 审批人配置ID
     * @return 阶段审批人数据对象
     */
    StageApproverDO selectById(@Param("id") Long id);

    /**
     * 动态条件查询阶段审批人列表
     *
     * @param query 查询条件
     * @return 阶段审批人数据对象列表
     */
    List<StageApproverDO> selectList(StageApproverQuery query);

    /**
     * 动态条件统计阶段审批人数量
     *
     * @param query 查询条件
     * @return 数量
     */
    Long selectCount(StageApproverQuery query);

    /**
     * 插入阶段审批人
     *
     * @param stageApproverDO 阶段审批人数据对象
     * @return 影响行数
     */
    int insert(StageApproverDO stageApproverDO);

    /**
     * 根据主键更新阶段审批人
     *
     * @param stageApproverDO 阶段审批人数据对象
     * @return 影响行数
     */
    int updateById(StageApproverDO stageApproverDO);

    /**
     * 根据主键删除阶段审批人（硬删除）
     *
     * @param id 审批人配置ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除阶段审批人（硬删除）
     *
     * @param query 查询条件
     * @return 影响行数
     */
    int delete(StageApproverQuery query);
}
