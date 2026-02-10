package com.xuanjiao.domain.workflow.repository;

import com.xuanjiao.domain.workflow.entity.Workflow;
import java.util.List;

/**
 * 工作流仓储接口
 *
 * <p>定义工作流定义的持久化操作。</p>
 * <p>工作流定义审批流程的结构，包括流程名称、版本、状态以及包含的阶段列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface WorkflowRepository {

    /**
     * 根据ID查找工作流
     *
     * @param id 工作流ID
     * @return 工作流实体，如果不存在返回 null
     */
    Workflow findById(Long id);

    /**
     * 查找所有工作流
     *
     * @return 所有工作流列表
     */
    List<Workflow> findAll();

    /**
     * 根据类型查找工作流列表
     *
     * @param type 工作流类型（ASSET_UPLOAD、ASSET_USAGE、ASSET_DELETION）
     * @return 匹配的工作流列表
     */
    List<Workflow> findByType(String type);

    /**
     * 保存工作流
     *
     * @param workflow 工作流实体
     */
    void save(Workflow workflow);

    /**
     * 更新工作流
     *
     * @param workflow 工作流实体
     */
    void update(Workflow workflow);

    /**
     * 根据ID删除工作流
     *
     * @param id 工作流ID
     */
    void deleteById(Long id);
}
