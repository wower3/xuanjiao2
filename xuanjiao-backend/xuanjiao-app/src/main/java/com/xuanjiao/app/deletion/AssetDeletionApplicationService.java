package com.xuanjiao.app.deletion;

import com.xuanjiao.client.dto.AssetDeletionApplicationCmd;
import com.xuanjiao.client.dto.AssetDeletionApplicationDTO;
import com.xuanjiao.client.dto.PageResult;

/**
 * 素材删除申请服务接口
 * <p>提供素材删除申请的创建、修改、提交、查询等功能</p>
 * <p>删除申请审批通过后，素材进入待清理状态，7天后彻底软删除</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.deletion.impl.AssetDeletionApplicationServiceImpl
 */
public interface AssetDeletionApplicationService {

    /**
     * 创建删除申请
     *
     * @param cmd 申请参数
     * @param userId 创建人ID
     * @return 申请单DTO
     */
    AssetDeletionApplicationDTO create(AssetDeletionApplicationCmd cmd, Long userId);

    /**
     * 更新删除申请
     *
     * @param id 申请单ID
     * @param cmd 更新参数
     * @return 申请单DTO
     */
    AssetDeletionApplicationDTO update(Long id, AssetDeletionApplicationCmd cmd);

    /**
     * 查询申请单详情
     *
     * @param id 申请单ID
     * @return 申请单DTO
     */
    AssetDeletionApplicationDTO getById(Long id);

    /**
     * 查询我的申请列表
     *
     * @param title 标题筛选（可选）
     * @param status 状态筛选（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<AssetDeletionApplicationDTO> getMyApplications(String title, String status, Integer pageNum, Integer pageSize, Long userId);

    /**
     * 查询草稿箱
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param title 标题筛选（可选）
     * @return 分页结果
     */
    PageResult<AssetDeletionApplicationDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize, String title);

    /**
     * 删除申请单
     *
     * @param id 申请单ID
     */
    void deleteById(Long id);

    /**
     * 提交删除申请进入审批流程
     *
     * @param id 申请单ID
     * @param workflowId 工作流ID
     * @param userId 操作人ID
     * @return 审批实例ID
     */
    Long submitApproval(Long id, Long workflowId, Long userId);

    /**
     * 更新申请单状态
     * <p>用于审批流程完成后调用</p>
     *
     * @param id 申请单ID
     * @param status 新状态
     */
    void updateStatus(Long id, String status);

    /**
     * 审批通过删除申请
     * <p>设置素材的删除审批通过时间</p>
     *
     * @param id 申请单ID
     */
    void approveDeletion(Long id);

    /**
     * 复制删除申请
     *
     * @param id 原申请单ID
     * @param userId 当前用户ID
     * @return 新申请单的ID
     */
    Long copyApplication(Long id, Long userId);
}
