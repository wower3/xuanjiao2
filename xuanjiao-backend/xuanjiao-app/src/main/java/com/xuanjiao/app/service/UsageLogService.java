package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.PageResult;
import java.util.Map;

public interface UsageLogService {
    void log(Long assetId, Long userId, String action, String ip);
    PageResult<Map<String, Object>> query(String action, int pageNum, int pageSize);
}
