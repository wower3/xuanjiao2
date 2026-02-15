package com.xuanjiao.app.usage;

import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.usage.UsageLogDTO;

/**
 * 素材使用日志服务接口
 *
 * <p>提供素材使用日志的记录和查询功能。记录用户对素材的下载和使用行为，
 * 用于审计和统计分析。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>记录素材使用日志（下载、预览等）</li>
 *   <li>查询使用日志列表</li>
 *   <li>查询指定素材的使用历史</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.usage.impl.UsageLogServiceImpl
 */
public interface UsageLogService {

    /**
     * 记录使用日志
     *
     * <p>记录用户对素材的操作行为。操作类型包括下载、预览等。</p>
     *
     * @param assetId 素材ID
     * @param userId 用户ID
     * @param action 操作类型（如：DOWNLOAD, PREVIEW）
     * @param ip 用户IP地址
     */
    void log(Long assetId, Long userId, String action, String ip);

    /**
     * 记录下载日志
     *
     * <p>记录用户下载素材的详细信息，包含使用说明和发布渠道。
     * 用于追踪素材的使用情况和版权管理。</p>
     *
     * @param assetId 素材ID
     * @param userId 用户ID
     * @param ip 用户IP地址
     * @param deptName 用户部门名称
     * @param usageDescription 使用说明（用户填写的使用目的）
     * @param usagePublishChannel 发布渠道（如：官网、微信公众号等）
     */
    void logDownload(Long assetId, Long userId, String ip, String deptName, String usageDescription, String usagePublishChannel);

    /**
     * 分页查询使用日志
     *
     * <p>查询系统中的素材使用日志记录。支持按操作类型筛选。</p>
     *
     * @param action 操作类型筛选（可选），如DOWNLOAD、PREVIEW，为null时查询全部
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果，包含日志详情、用户信息、素材信息等
     */
    PageResult<UsageLogDTO> query(String action, int pageNum, int pageSize);

    /**
     * 获取素材的使用日志
     *
     * <p>查询指定素材的所有使用记录。用于查看素材被谁使用过。</p>
     *
     * @param assetId 素材ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<UsageLogDTO> getAssetUsageLogs(Long assetId, int pageNum, int pageSize);
}
