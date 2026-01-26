package com.xuanjiao.app.usage;

import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.UsageLogDTO;
import java.util.Map;

public interface UsageLogService {
    void log(Long assetId, Long userId, String action, String ip);
    void logDownload(Long assetId, Long userId, String ip, String deptName, String usageDescription, String usagePublishChannel);
    PageResult<Map<String, Object>> query(String action, int pageNum, int pageSize);
    PageResult<UsageLogDTO> getAssetUsageLogs(Long assetId, int pageNum, int pageSize);
}
