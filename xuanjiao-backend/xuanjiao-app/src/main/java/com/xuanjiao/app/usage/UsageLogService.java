package com.xuanjiao.app.usage;

import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.UsageLogDTO;
import java.util.Map;

/**
 * 素材使用日志服务接口
 * <p>提供素材使用日志的记录和查询功能</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.usage.impl.UsageLogServiceImpl
 */
public interface UsageLogService {

    /**
     * 记录使用日志
     *
     * @param assetId 素材ID
     * @param userId 用户ID
     * @param action 操作类型
     * @param ip IP地址
     */
    void log(Long assetId, Long userId, String action, String ip);

    /**
     * 记录下载日志
     *
     * @param assetId 素材ID
     * @param userId 用户ID
     * @param ip IP地址
     * @param deptName 部门名称
     * @param usageDescription 使用说明
     * @param usagePublishChannel 发布渠道
     */
    void logDownload(Long assetId, Long userId, String ip, String deptName, String usageDescription, String usagePublishChannel);

    /**
     * 分页查询使用日志
     *
     * @param action 操作类型筛选
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<Map<String, Object>> query(String action, int pageNum, int pageSize);

    /**
     * 获取素材的使用日志
     *
     * @param assetId 素材ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<UsageLogDTO> getAssetUsageLogs(Long assetId, int pageNum, int pageSize);
}
