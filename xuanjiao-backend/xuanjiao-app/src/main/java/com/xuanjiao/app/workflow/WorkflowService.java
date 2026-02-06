package com.xuanjiao.app.workflow;

import com.xuanjiao.client.dto.WorkflowDTO;
import java.util.List;

/**
 * 工作流定义服务接口
 * <p>提供工作流的增删改查、状态管理、角色绑定等功能</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.workflow.impl.WorkflowServiceImpl
 */
public interface WorkflowService {

    /**
     * 获取所有启用的工作流列表
     *
     * @return 工作流DTO列表
     */
    List<WorkflowDTO> list();

    /**
     * 根据ID获取工作流详情
     *
     * @param id 工作流ID
     * @return 工作流DTO
     */
    WorkflowDTO getById(Long id);

    /**
     * 保存工作流
     *
     * @param dto 工作流DTO
     * @return 保存后的工作流DTO
     */
    WorkflowDTO save(WorkflowDTO dto);

    /**
     * 更新工作流
     *
     * @param dto 工作流DTO
     */
    void update(WorkflowDTO dto);

    /**
     * 删除工作流
     *
     * @param id 工作流ID
     */
    void delete(Long id);

    /**
     * 更新工作流状态
     *
     * @param id 工作流ID
     * @param status 新状态：1-启用、0-停用
     */
    void updateStatus(Long id, Integer status);

    /**
     * 绑定角色
     *
     * @param id 工作流ID
     * @param roleId 角色ID
     * @param workflowType 流程类型
     */
    void bindRole(Long id, Long roleId, String workflowType);

    /**
     * 解绑角色
     *
     * @param id 工作流ID
     */
    void unbindRole(Long id);

    /**
     * 复制工作流
     *
     * @param id 原工作流ID
     * @return 复制后的新工作流DTO
     */
    WorkflowDTO copy(Long id);
}
