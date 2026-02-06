package com.xuanjiao.app.asset;

import com.xuanjiao.client.dto.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 素材服务接口
 * <p>提供素材的查询、上传、下载、标签管理等功能</p>
 * <p>素材操作涉及审批流程：录入需审批、删除需审批</p>
 * <p>素材状态变更：PENDING → APPROVED/REJECTED</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.asset.impl.AssetServiceImpl
 */
public interface AssetService {

    /**
     * 上传素材
     * <p>处理文件上传，支持主图和缩略图，保存素材记录</p>
     *
     * @param file 主文件
     * @param thumbnailFile 缩略图文件（可选）
     * @param cmd 上传参数
     * @param userId 上传用户ID
     * @return 上传后的素材信息
     */
    AssetDTO upload(MultipartFile file, MultipartFile thumbnailFile, AssetUploadCmd cmd, Long userId);

    /**
     * 根据ID获取素材
     *
     * @param id 素材ID
     * @return 素材信息，不存在返回null
     */
    AssetDTO getById(Long id);

    /**
     * 查询素材列表（带权限过滤）
     *
     * @param cmd 查询参数
     * @return 分页结果
     */
    PageResult<AssetDTO> query(AssetQueryCmd cmd);

    /**
     * 查询素材列表（带角色权限过滤）
     *
     * @param cmd 查询参数
     * @param userId 当前用户ID
     * @return 分页结果
     */
    PageResult<AssetDTO> queryWithRoleFilter(AssetQueryCmd cmd, Long userId);

    /**
     * 获取我已审批通过的素材列表
     *
     * @param name 素材名称（模糊查询）
     * @param type 素材类型
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<AssetDTO> getMyApprovedAssets(String name, String type, Integer pageNum, Integer pageSize, Long userId);

    /**
     * 删除素材（逻辑删除）
     *
     * @param id 素材ID
     */
    void delete(Long id);

    /**
     * 根据申请ID批量更新素材状态
     *
     * @param applicationId 申请ID
     * @param status 新状态
     */
    void updateStatusByApplicationId(Long applicationId, String status);

    /**
     * 管理员彻底删除素材（不需要审批）
     *
     * @param assetId 素材ID
     * @param reason 删除理由（必填）
     * @param userId 操作人ID
     * @param isAdmin 是否管理员
     */
    void adminDelete(Long assetId, String reason, Long userId, Boolean isAdmin);

    /**
     * 管理员调整素材删除时间（测试功能）
     * 将删除审批通过时间改为一周前，以便定时任务清理
     *
     * @param assetId 素材ID
     * @param isAdmin 是否管理员
     */
    void adjustDeleteTime(Long assetId, Boolean isAdmin);

    /**
     * 手动触发定时任务（测试功能）
     * 执行素材彻底软删除定时任务
     *
     * @param isAdmin 是否管理员
     * @return 删除的素材数量
     */
    int triggerCleanupTask(Boolean isAdmin);
}
