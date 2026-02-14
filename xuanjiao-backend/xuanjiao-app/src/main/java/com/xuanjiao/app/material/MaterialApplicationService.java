package com.xuanjiao.app.material;

import com.xuanjiao.client.MaterialApplicationCmd;
import com.xuanjiao.client.MaterialApplicationDTO;
import com.xuanjiao.client.PageResult;

/**
 * 素材录入申请服务接口
 *
 * <p>提供素材录入申请的创建、修改、提交、查询等功能。
 * 素材录入申请用于管理企业媒体资产的入库流程。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>申请单CRUD操作</li>
 *   <li>草稿箱管理</li>
 *   <li>提交审批流程</li>
 *   <li>申请单复制</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.material.impl.MaterialApplicationServiceImpl
 */
public interface MaterialApplicationService {

    /**
     * 创建素材申请单（草稿）
     *
     * <p>创建一个新的素材录入申请单，初始状态为DRAFT。
     * 用户可以在草稿状态下上传素材文件，完善申请信息。</p>
     *
     * @param cmd 申请参数，包含标题、维护人、部门等信息
     * @param userId 创建人ID
     * @return 创建的申请单DTO
     */
    MaterialApplicationDTO create(MaterialApplicationCmd cmd, Long userId);

    /**
     * 更新素材申请单
     *
     * <p>更新已有申请单的信息。只能更新DRAFT状态的申请单，
     * 且只能更新自己的申请单。</p>
     *
     * @param id 申请单ID
     * @param cmd 更新参数
     * @param userId 操作人ID
     * @return 更新后的申请单DTO
     * @throws RuntimeException 如果申请单不存在、非草稿状态或非本人申请单
     */
    MaterialApplicationDTO update(Long id, MaterialApplicationCmd cmd, Long userId);

    /**
     * 提交申请单进入审批流程
     *
     * <p>将草稿或已驳回状态的申请单提交审批。提交前会验证：
     * <ul>
     *   <li>申请单必须存在</li>
     *   <li>状态必须为DRAFT或REJECTED</li>
     *   <li>必须至少上传一个素材文件</li>
     *   <li>只能提交自己的申请单</li>
     * </ul></p>
     *
     * @param id 申请单ID
     * @param workflowId 工作流ID
     * @param userId 操作人ID
     * @return 审批实例ID
     * @throws RuntimeException 如果验证失败
     */
    Long submit(Long id, Long workflowId, Long userId);

    /**
     * 删除申请单（仅草稿状态）
     *
     * <p>删除申请单及其关联的所有素材文件。只能删除DRAFT状态的申请单，
     * 且只能删除自己的申请单。</p>
     *
     * @param id 申请单ID
     * @param userId 操作人ID
     * @throws RuntimeException 如果申请单不存在、非草稿状态或非本人申请单
     */
    void delete(Long id, Long userId);

    /**
     * 查询申请单详情
     *
     * <p>返回申请单的完整信息，包含申请人、维护人、部门、关联素材等。</p>
     *
     * @param id 申请单ID
     * @return 申请单DTO，不存在返回null
     */
    MaterialApplicationDTO getById(Long id);

    /**
     * 查询草稿箱
     *
     * <p>返回当前用户的所有DRAFT状态的申请单。</p>
     *
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<MaterialApplicationDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询草稿箱（支持标题筛选）
     *
     * <p>返回当前用户的所有DRAFT状态的申请单，支持按标题模糊查询。</p>
     *
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param title 标题筛选（模糊查询），可为null
     * @return 分页结果
     */
    PageResult<MaterialApplicationDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize, String title);

    /**
     * 查询我的申请单
     *
     * <p>返回当前用户的所有申请单（包含所有状态）。</p>
     *
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<MaterialApplicationDTO> queryMyApplications(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 更新申请单状态
     *
     * <p>用于审批流程完成后调用，更新申请单的最终状态。
     * 通常由WorkflowCompletionHandler调用。</p>
     *
     * @param id 申请单ID
     * @param status 新状态（APPROVED或REJECTED）
     */
    void updateStatus(Long id, String status);

    /**
     * 复制申请单
     *
     * <p>复制原申请单及其关联的素材信息到新草稿。复制内容包括：
     * <ul>
     *   <li>申请单基本信息</li>
     *   <li>所有素材文件（包括物理文件复制）</li>
     *   <li>素材标签关联</li>
     * </ul></p>
     *
     * @param id 原申请单ID
     * @param userId 当前用户ID
     * @return 新申请单的ID
     * @throws RuntimeException 如果原申请单不存在
     */
    Long copyApplication(Long id, Long userId);
}
