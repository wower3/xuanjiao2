package com.xuanjiao.app.deletion;

import com.xuanjiao.client.dto.deletion.AssetDeletionApplicationCmd;
import com.xuanjiao.client.dto.deletion.dto.AssetDeletionApplicationDTO;
import com.xuanjiao.client.dto.common.PageResult;

/**
 * 素材删除申请服务接口
 *
 * <p>提供素材删除申请的创建、修改、提交、查询等功能。
 * 删除申请审批通过后，素材进入待清理状态，7天后彻底软删除。</p>
 *
 * <p>删除流程：</p>
 * <ol>
 *   <li>用户创建删除申请并提交审批</li>
 *   <li>审批通过后素材状态变为DELETED（可见但不可用）</li>
 *   <li>7天后定时任务执行软删除（deleted=1，完全隐藏）</li>
 * </ol>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.deletion.impl.AssetDeletionApplicationServiceImpl
 */
public interface AssetDeletionApplicationService {

    /**
     * 创建删除申请
     *
     * <p>创建一个新的素材删除申请单，可选择关联要删除的素材。
     * 只能删除APPROVED状态的素材。</p>
     *
     * @param cmd 申请参数，包含标题、删除原因、素材ID列表等
     * @param userId 创建人ID
     * @return 申请单DTO
     * @throws RuntimeException 如果素材不存在或状态非APPROVED
     */
    AssetDeletionApplicationDTO create(AssetDeletionApplicationCmd cmd, Long userId);

    /**
     * 更新删除申请
     *
     * <p>更新已有删除申请的信息。只能更新DRAFT状态的申请单。</p>
     *
     * @param id 申请单ID
     * @param cmd 更新参数
     * @return 申请单DTO
     * @throws RuntimeException 如果申请单不存在或非草稿状态
     */
    AssetDeletionApplicationDTO update(Long id, AssetDeletionApplicationCmd cmd);

    /**
     * 查询申请单详情
     *
     * <p>返回删除申请单的完整信息，包含申请人、删除原因、关联素材等。</p>
     *
     * @param id 申请单ID
     * @return 申请单DTO，不存在返回null
     */
    AssetDeletionApplicationDTO getById(Long id);

    /**
     * 查询我的申请列表
     *
     * <p>返回当前用户的所有删除申请（包含所有状态），支持多条件筛选。</p>
     *
     * @param title 标题筛选（可选），支持模糊查询
     * @param status 状态筛选（可选），如DRAFT、PENDING、APPROVED、REJECTED
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<AssetDeletionApplicationDTO> getMyApplications(String title, String status, Integer pageNum, Integer pageSize, Long userId);

    /**
     * 查询草稿箱
     *
     * <p>返回当前用户的所有DRAFT状态的删除申请。</p>
     *
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param title 标题筛选（可选），支持模糊查询
     * @return 分页结果
     */
    PageResult<AssetDeletionApplicationDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize, String title);

    /**
     * 删除申请单
     *
     * <p>删除删除申请单。只能删除DRAFT或REJECTED状态的申请单。</p>
     *
     * @param id 申请单ID
     * @throws RuntimeException 如果申请单不存在或状态不允许删除
     */
    void deleteById(Long id);

    /**
     * 提交删除申请进入审批流程
     *
     * <p>将草稿或已驳回状态的申请单提交审批。提交前会验证：
     * <ul>
     *   <li>申请单必须存在</li>
     *   <li>状态必须为DRAFT或REJECTED</li>
     *   <li>只能提交自己的申请单</li>
     * </ul></p>
     *
     * @param id 申请单ID
     * @param workflowId 工作流ID
     * @param userId 操作人ID
     * @return 审批实例ID
     * @throws RuntimeException 如果验证失败
     */
    Long submitApproval(Long id, Long workflowId, Long userId);

    /**
     * 更新申请单状态
     *
     * <p>用于审批流程完成后调用，更新申请单的最终状态。</p>
     *
     * @param id 申请单ID
     * @param status 新状态（APPROVED或REJECTED）
     */
    void updateStatus(Long id, String status);

    /**
     * 审批通过删除申请
     *
     * <p>当删除申请审批通过时调用，执行以下操作：
     * <ul>
     *   <li>将所有关联素材状态设置为DELETED</li>
     *   <li>记录素材删除审批通过时间（deletion_approve_time）</li>
     *   <li>7天后定时任务将执行软删除</li>
     * </ul></p>
     *
     * @param id 申请单ID
     */
    void approveDeletion(Long id);

    /**
     * 复制删除申请
     *
     * <p>复制原删除申请到新草稿。只复制申请单信息和素材关联引用，
     * 不复制素材文件本身。</p>
     *
     * @param id 原申请单ID
     * @param userId 当前用户ID
     * @return 新申请单的ID
     * @throws RuntimeException 如果原申请单不存在
     */
    Long copyApplication(Long id, Long userId);
}
