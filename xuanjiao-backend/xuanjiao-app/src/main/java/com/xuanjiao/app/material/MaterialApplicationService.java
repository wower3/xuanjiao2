package com.xuanjiao.app.material;

import com.xuanjiao.client.dto.MaterialApplicationCmd;
import com.xuanjiao.client.dto.MaterialApplicationDTO;
import com.xuanjiao.client.dto.PageResult;

/**
 * 素材录入申请服务接口
 * <p>提供素材录入申请的创建、修改、提交、查询等功能</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.material.impl.MaterialApplicationServiceImpl
 */
public interface MaterialApplicationService {

    /**
     * 创建素材申请单（草稿）
     *
     * @param cmd 申请参数
     * @param userId 创建人ID
     * @return 创建的申请单DTO
     */
    MaterialApplicationDTO create(MaterialApplicationCmd cmd, Long userId);

    /**
     * 更新素材申请单
     *
     * @param id 申请单ID
     * @param cmd 更新参数
     * @param userId 操作人ID
     * @return 更新后的申请单DTO
     */
    MaterialApplicationDTO update(Long id, MaterialApplicationCmd cmd, Long userId);

    /**
     * 提交申请单进入审批流程
     *
     * @param id 申请单ID
     * @param workflowId 工作流ID
     * @param userId 操作人ID
     * @return 审批实例ID
     */
    Long submit(Long id, Long workflowId, Long userId);

    /**
     * 删除申请单（仅草稿状态）
     *
     * @param id 申请单ID
     * @param userId 操作人ID
     */
    void delete(Long id, Long userId);

    /**
     * 查询申请单详情
     *
     * @param id 申请单ID
     * @return 申请单DTO
     */
    MaterialApplicationDTO getById(Long id);

    /**
     * 查询草稿箱
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<MaterialApplicationDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询草稿箱（支持标题筛选）
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param title 标题筛选（模糊查询）
     * @return 分页结果
     */
    PageResult<MaterialApplicationDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize, String title);

    /**
     * 查询我的申请单
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<MaterialApplicationDTO> queryMyApplications(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 更新申请单状态
     * <p>用于审批流程完成后调用</p>
     *
     * @param id 申请单ID
     * @param status 新状态
     */
    void updateStatus(Long id, String status);

    /**
     * 复制申请单
     * <p>复制原申请单的素材信息到新草稿</p>
     *
     * @param id 原申请单ID
     * @param userId 当前用户ID
     * @return 新申请单的ID
     */
    Long copyApplication(Long id, Long userId);
}
