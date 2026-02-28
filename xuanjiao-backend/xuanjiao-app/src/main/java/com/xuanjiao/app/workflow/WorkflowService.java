package com.xuanjiao.app.workflow;

import com.xuanjiao.client.workflow.WorkflowDTO;
import java.util.List;

/**
 * 工作流定义服务接口
 *
 * <p>提供工作流的增删改查、状态管理、角色绑定等功能。
 * 工作流定义包含阶段配置、审批人配置、子流程配置等。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>工作流CRUD操作</li>
 *   <li>工作流状态管理（启用/停用）</li>
 *   <li>角色绑定（确定哪种角色使用哪个流程）</li>
 *   <li>工作流复制</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
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
     * @return 工作流DTO，不存在返回null
     */
    WorkflowDTO getById(Long id);

    /**
     * 保存工作流
     *
     * <p>创建新的工作流定义，包含阶段、审批人等配置信息。</p>
     *
     * @param dto 工作流DTO
     * @return 保存后的工作流DTO（包含生成的ID）
     */
    WorkflowDTO save(WorkflowDTO dto);

    /**
     * 更新工作流
     *
     * <p>更新已有的工作流定义。注意：正在使用中的工作流不建议修改。</p>
     *
     * @param dto 工作流DTO
     */
    void update(WorkflowDTO dto);

    /**
     * 删除工作流
     *
     * <p>删除工作流定义。注意：正在使用中的工作流不允许删除。</p>
     *
     * @param id 工作流ID
     */
    void delete(Long id);

    /**
     * 更新工作流状态
     *
     * <p>启用或停用工作流。停用后，新申请将无法使用该流程。</p>
     *
     * @param id 工作流ID
     * @param status 新状态：1-启用、0-停用
     */
    void updateStatus(Long id, Integer status);

    /**
     * 绑定角色
     *
     * <p>将工作流绑定到指定角色，确定该角色使用的审批流程类型。</p>
     *
     * @param id 工作流ID
     * @param roleId 角色ID
     * @param workflowType 流程类型（如：MATERIAL_ENTRY, ASSET_USAGE, ASSET_DELETION）
     */
    void bindRole(Long id, Long roleId, String workflowType);

    /**
     * 解绑角色
     *
     * <p>解除工作流与角色的绑定关系。</p>
     *
     * @param id 工作流ID
     */
    void unbindRole(Long id);

    /**
     * 复制工作流
     *
     * <p>复制现有工作流创建新工作流，包含所有阶段和审批人配置。</p>
     *
     * @param id 原工作流ID
     * @return 复制后的新工作流DTO
     */
    WorkflowDTO copy(Long id);
}
