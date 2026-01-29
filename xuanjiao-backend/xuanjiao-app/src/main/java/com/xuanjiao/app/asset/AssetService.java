package com.xuanjiao.app.asset;

import com.xuanjiao.client.dto.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface AssetService {
    AssetDTO upload(MultipartFile file, AssetUploadCmd cmd, Long userId);
    AssetDTO getById(Long id);
    PageResult<AssetDTO> query(AssetQueryCmd cmd);
    PageResult<AssetDTO> queryWithRoleFilter(AssetQueryCmd cmd, Long userId);
    PageResult<AssetDTO> getMyApprovedAssets(String name, String type, Integer pageNum, Integer pageSize, Long userId);
    void delete(Long id);
    void updateStatusByApplicationId(Long applicationId, String status);

    /**
     * 管理员彻底删除素材（不需要审批）
     * @param assetId 素材ID
     * @param reason 删除理由（必填）
     * @param userId 操作人ID
     * @param isAdmin 是否管理员
     */
    void adminDelete(Long assetId, String reason, Long userId, Boolean isAdmin);

    /**
     * 管理员调整素材删除时间（测试功能）
     * 将删除审批通过时间改为一周前，以便定时任务清理
     * @param assetId 素材ID
     * @param isAdmin 是否管理员
     */
    void adjustDeleteTime(Long assetId, Boolean isAdmin);

    /**
     * 手动触发定时任务（测试功能）
     * 执行素材彻底软删除定时任务
     * @param isAdmin 是否管理员
     * @return 删除的素材数量
     */
    int triggerCleanupTask(Boolean isAdmin);
}
