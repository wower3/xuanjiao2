package com.xuanjiao.app.material;

import com.xuanjiao.client.dto.MaterialApplicationCmd;
import com.xuanjiao.client.dto.MaterialApplicationDTO;
import com.xuanjiao.client.dto.PageResult;

public interface MaterialApplicationService {
    /**
     * 创建素材申请单（草稿）
     */
    MaterialApplicationDTO create(MaterialApplicationCmd cmd, Long userId);

    /**
     * 更新素材申请单
     */
    MaterialApplicationDTO update(Long id, MaterialApplicationCmd cmd, Long userId);

    /**
     * 提交申请单进入审批流程
     * @return 审批实例ID
     */
    Long submit(Long id, Long workflowId, Long userId);

    /**
     * 删除申请单（仅草稿状态）
     */
    void delete(Long id, Long userId);

    /**
     * 查询申请单详情
     */
    MaterialApplicationDTO getById(Long id);

    /**
     * 查询草稿箱
     */
    PageResult<MaterialApplicationDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询我的申请单
     */
    PageResult<MaterialApplicationDTO> queryMyApplications(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 更新申请单状态（用于审批流程完成后调用）
     */
    void updateStatus(Long id, String status);

    /**
     * 复制申请单（复制原申请单的素材信息到新草稿）
     * @param id 原申请单ID
     * @param userId 当前用户ID
     * @return 新申请单的ID
     */
    Long copyApplication(Long id, Long userId);
}
