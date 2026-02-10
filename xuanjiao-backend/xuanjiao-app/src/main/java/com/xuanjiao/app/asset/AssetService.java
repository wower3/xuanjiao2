package com.xuanjiao.app.asset;

import com.xuanjiao.client.dto.AssetDTO;
import com.xuanjiao.client.dto.AssetQueryCmd;
import com.xuanjiao.client.dto.AssetUploadCmd;
import com.xuanjiao.client.dto.PageResult;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 素材服务接口
 *
 * <p>提供素材的查询、上传、下载、标签管理等功能。
 * 素材操作涉及审批流程：录入需审批、删除需审批。</p>
 *
 * <p>素材状态流转：</p>
 * <ul>
 *   <li>PENDING - 待审批（新上传素材）</li>
 *   <li>APPROVED - 已通过（可正常使用）</li>
 *   <li>REJECTED - 已驳回</li>
 *   <li>DELETED - 已删除（可见但不可用，7天后彻底软删除）</li>
 * </ul>
 *
 * <p>管理员特殊功能：</p>
 * <ul>
 *   <li>直接删除素材（无需审批）</li>
 *   <li>调整删除时间（测试定时任务）</li>
 *   <li>手动触发清理任务</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.asset.impl.AssetServiceImpl
 */
public interface AssetService {

    /**
     * 上传素材
     *
     * <p>处理文件上传，支持主图和缩略图，保存素材记录。
     * 上传的素材初始状态为PENDING，需要通过审批才能使用。
     * 文件使用MD5去重，相同文件只存储一份。</p>
     *
     * @param file 主文件（必填）
     * @param thumbnailFile 缩略图文件（可选，主要用于视频素材）
     * @param cmd 上传参数，包含名称、类型、标签、申请ID等
     * @param userId 上传用户ID
     * @return 上传后的素材信息
     * @throws RuntimeException 如果文件格式不支持或上传失败
     */
    AssetDTO upload(MultipartFile file, MultipartFile thumbnailFile, AssetUploadCmd cmd, Long userId);

    /**
     * 根据ID获取素材
     *
     * <p>返回指定素材的详细信息，包含标签、维护人、部门等。</p>
     *
     * @param id 素材ID
     * @return 素材信息，不存在返回null
     */
    AssetDTO getById(Long id);

    /**
     * 查询素材列表（带权限过滤）
     *
     * <p>查询所有可见的素材。自动过滤已软删除（deleted=1）的素材。</p>
     *
     * @param cmd 查询参数，支持名称、类型、状态、标签等筛选
     * @return 分页结果
     */
    PageResult<AssetDTO> query(AssetQueryCmd cmd);

    /**
     * 查询素材列表（带角色权限过滤）
     *
     * <p>根据用户角色过滤可见范围：
     * <ul>
     *   <li>系统管理员/总消保管理岗：可查看所有素材</li>
     *   <li>分消保管理岗：只能查看所属分部及子部门的素材</li>
     *   <li>普通用户：查看所有审批通过的素材</li>
     * </ul></p>
     *
     * @param cmd 查询参数
     * @param userId 当前用户ID
     * @return 分页结果
     */
    PageResult<AssetDTO> queryWithRoleFilter(AssetQueryCmd cmd, Long userId);

    /**
     * 获取我已审批通过的素材列表
     *
     * <p>返回当前用户可使用的素材（状态为APPROVED）。
     * 用于素材使用申请时选择素材。</p>
     *
     * @param name 素材名称（模糊查询），可为null
     * @param type 素材类型，可为null
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<AssetDTO> getMyApprovedAssets(String name, String type, Integer pageNum, Integer pageSize, Long userId);

    /**
     * 删除素材（逻辑删除）
     *
     * <p>设置素材的deleted标志为1，物理文件不删除。
     * 此方法仅供内部调用，用户删除需要通过审批流程。</p>
     *
     * @param id 素材ID
     */
    void delete(Long id);

    /**
     * 根据申请ID批量更新素材状态
     *
     * <p>当素材录入申请审批通过或驳回时，批量更新关联素材的状态。
     * 通常由MaterialEntryHandler调用。</p>
     *
     * @param applicationId 素材录入申请ID
     * @param status 新状态（APPROVED或REJECTED）
     */
    void updateStatusByApplicationId(Long applicationId, String status);

    /**
     * 管理员彻底删除素材（不需要审批）
     *
     * <p>管理员可直接删除素材，跳过审批流程。
     * 删除后素材状态变为DELETED，7天后执行软删除。</p>
     *
     * @param assetId 素材ID
     * @param reason 删除理由（必填）
     * @param userId 操作人ID
     * @param isAdmin 是否管理员（必须为true才能执行）
     * @throws RuntimeException 如果非管理员或素材不存在
     */
    void adminDelete(Long assetId, String reason, Long userId, Boolean isAdmin);

    /**
     * 管理员调整素材删除时间（测试功能）
     *
     * <p>将素材的删除审批通过时间改为一周前，以便定时任务可以立即清理。
     * 仅用于测试环境验证定时任务功能。</p>
     *
     * @param assetId 素材ID（必须为DELETED状态）
     * @param isAdmin 是否管理员（必须为true才能执行）
     * @throws RuntimeException 如果非管理员或素材状态非DELETED
     */
    void adjustDeleteTime(Long assetId, Boolean isAdmin);

    /**
     * 手动触发定时任务（测试功能）
     *
     * <p>立即执行素材彻底软删除定时任务。
     * 仅用于测试环境验证定时任务功能。</p>
     *
     * @param isAdmin 是否管理员（必须为true才能执行）
     * @return 删除的素材数量
     * @throws RuntimeException 如果非管理员
     */
    int triggerCleanupTask(Boolean isAdmin);
}
