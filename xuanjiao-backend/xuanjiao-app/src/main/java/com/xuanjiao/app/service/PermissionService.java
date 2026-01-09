package com.xuanjiao.app.service;

import java.util.List;
import java.util.Map;

public interface PermissionService {
    List<Map<String, Object>> getUserPermissions(Long userId);
    List<String> getUserPermissionCodes(Long userId);
}
