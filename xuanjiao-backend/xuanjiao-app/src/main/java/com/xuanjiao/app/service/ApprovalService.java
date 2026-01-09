package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.PageResult;
import java.util.Map;

public interface ApprovalService {
    PageResult<Map<String, Object>> getMyTasks(Long userId, int pageNum, int pageSize);
    PageResult<Map<String, Object>> getMyApplied(Long userId, int pageNum, int pageSize);
    void approve(Long taskId, Long userId, String comment, boolean passed);
}
