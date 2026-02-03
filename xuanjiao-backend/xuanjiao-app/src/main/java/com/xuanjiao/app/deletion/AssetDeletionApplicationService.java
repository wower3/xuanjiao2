package com.xuanjiao.app.deletion;

import com.xuanjiao.client.dto.AssetDeletionApplicationCmd;
import com.xuanjiao.client.dto.AssetDeletionApplicationDTO;
import com.xuanjiao.client.dto.PageResult;

/**
 * 素材删除申请Service接口
 */
public interface AssetDeletionApplicationService {
    AssetDeletionApplicationDTO create(AssetDeletionApplicationCmd cmd, Long userId);
    AssetDeletionApplicationDTO update(Long id, AssetDeletionApplicationCmd cmd);
    AssetDeletionApplicationDTO getById(Long id);
    PageResult<AssetDeletionApplicationDTO> getMyApplications(String title, String status, Integer pageNum, Integer pageSize, Long userId);
    PageResult<AssetDeletionApplicationDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize, String title);
    void deleteById(Long id);
    Long submitApproval(Long id, Long workflowId, Long userId);
    void updateStatus(Long id, String status);
    void approveDeletion(Long id);
    Long copyApplication(Long id, Long userId);
}
