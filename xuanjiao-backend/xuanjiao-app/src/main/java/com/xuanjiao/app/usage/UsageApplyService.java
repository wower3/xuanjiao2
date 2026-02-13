package com.xuanjiao.app.usage;

import com.xuanjiao.client.dto.common.PageResult;
import com.xuanjiao.client.dto.usage.UsageApplyCmd;
import com.xuanjiao.client.dto.usage.dto.UsageApplyDTO;
import com.xuanjiao.client.dto.usage.UsageApplyQry;

/**
 * 素材使用申请服务接口
 *
 * <p>提供素材使用申请的创建、修改、提交、查询等功能。
 * 支持多素材申请，一个申请可以包含多个素材。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>使用申请CRUD操作</li>
 *   <li>草稿箱管理</li>
 *   <li>提交审批流程</li>
 *   <li>素材使用权限检查</li>
 * </ul>
 *
 * <p>状态流转：</p>
 * <ul>
 *   <li>DRAFT - 草稿</li>
 *   <li>PENDING - 待审批</li>
 *   <li>APPROVED - 已通过（可下载使用素材）</li>
 *   <li>REJECTED - 已驳回</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.usage.impl.UsageApplyServiceImpl
 */
public interface UsageApplyService {

    /**
     * 申请使用素材（旧API，兼容单素材申请）
     *
     * <p>创建并立即提交素材使用申请。仅支持单个素材。
     * 建议使用createDraft + submit的组合方式。</p>
     *
     * @param cmd 申请参数
     * @param userId 申请人ID
     * @return 申请单DTO
     * @deprecated 建议使用 {@link #createDraft} + {@link #submit}
     */
    UsageApplyDTO apply(UsageApplyCmd cmd, Long userId);

    /**
     * 创建使用申请草稿（多素材支持）
     *
     * <p>创建素材使用申请草稿。支持同时申请多个素材，
     * 每个素材可以有独立的使用说明、发布渠道等配置。</p>
     *
     * @param cmd 申请参数，包含标题、素材列表、使用配置等
     * @param userId 创建人ID
     * @return 申请单DTO
     */
    UsageApplyDTO createDraft(UsageApplyCmd cmd, Long userId);

    /**
     * 更新使用申请草稿
     *
     * <p>更新已有草稿的信息。只能更新DRAFT状态的申请单。</p>
     *
     * @param id 申请单ID
     * @param cmd 更新参数
     * @param userId 操作人ID
     * @return 更新后的申请单DTO
     * @throws RuntimeException 如果申请单不存在、非草稿状态或非本人申请单
     */
    UsageApplyDTO updateDraft(Long id, UsageApplyCmd cmd, Long userId);

    /**
     * 提交使用申请（从草稿状态提交到审批）
     *
     * <p>将草稿状态的申请单提交审批。提交前会验证：
     * <ul>
     *   <li>申请单必须存在</li>
     *   <li>状态必须为DRAFT或REJECTED</li>
     *   <li>必须至少选择一个素材</li>
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
     * 删除使用申请（仅草稿状态）
     *
     * <p>删除申请单及其关联的素材配置。只能删除DRAFT状态的申请单。</p>
     *
     * @param id 申请单ID
     * @param userId 操作人ID
     * @throws RuntimeException 如果申请单不存在或非草稿状态
     */
    void delete(Long id, Long userId);

    /**
     * 查询申请单详情
     *
     * <p>返回使用申请单的完整信息，包含申请人、素材列表、使用配置等。</p>
     *
     * @param id 申请单ID
     * @return 申请单DTO，不存在返回null
     */
    UsageApplyDTO getById(Long id);

    /**
     * 查询草稿箱
     *
     * <p>返回当前用户的所有DRAFT状态的使用申请。</p>
     *
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<UsageApplyDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询草稿箱（支持标题筛选）
     *
     * <p>返回当前用户的所有DRAFT状态的使用申请，支持按标题模糊查询。</p>
     *
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param title 标题筛选（模糊查询），可为null
     * @return 分页结果
     */
    PageResult<UsageApplyDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize, String title);

    /**
     * 查询我的申请列表（全部）
     *
     * <p>返回当前用户的所有使用申请（包含所有状态）。</p>
     *
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<UsageApplyDTO> queryMyApplications(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询我的申请列表（旧API，保持兼容）
     *
     * <p>返回当前用户的使用申请，支持多条件筛选。</p>
     *
     * @param cmd 查询参数
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<UsageApplyDTO> queryMyApplications(UsageApplyQry cmd, Long userId);

    /**
     * 检查用户是否有权限使用素材
     *
     * <p>检查用户是否有已通过的使用申请可以使用该素材。
     * 只有APPROVED状态的使用申请才被认为有权限。</p>
     *
     * @param assetId 素材ID
     * @param userId 用户ID
     * @return 如果有权限返回true，否则返回false
     */
    boolean canUseAsset(Long assetId, Long userId);

    /**
     * 更新申请单状态
     *
     * <p>用于审批流程完成后调用，更新申请单的最终状态。
     * 通常由AssetUsageHandler调用。</p>
     *
     * @param id 申请单ID
     * @param status 新状态（APPROVED或REJECTED）
     */
    void updateStatus(Long id, String status);

    /**
     * 复制使用申请
     *
     * <p>复制原使用申请到新草稿。复制内容包括申请单信息和素材配置。
     * 只能复制REJECTED状态的申请单。</p>
     *
     * @param id 原申请单ID
     * @param userId 当前用户ID
     * @return 新申请单的ID
     * @throws RuntimeException 如果原申请单不存在或状态不允许复制
     */
    Long copyApplication(Long id, Long userId);
}
