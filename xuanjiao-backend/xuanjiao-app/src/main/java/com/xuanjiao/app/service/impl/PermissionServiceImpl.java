package com.xuanjiao.app.service.impl;

import com.xuanjiao.app.service.PermissionService;
import com.xuanjiao.infrastructure.dataobject.PermissionDO;
import com.xuanjiao.infrastructure.mapper.PermissionMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private PermissionMapper permissionMapper;

    @Override
    public List<Map<String, Object>> getUserPermissions(Long userId) {
        List<PermissionDO> permissions = permissionMapper.selectByUserId(userId);
        return permissions.stream().map(this::toMap).collect(Collectors.toList());
    }

    @Override
    public List<String> getUserPermissionCodes(Long userId) {
        List<PermissionDO> permissions = permissionMapper.selectByUserId(userId);
        return permissions.stream().map(PermissionDO::getCode).collect(Collectors.toList());
    }

    private Map<String, Object> toMap(PermissionDO p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("code", p.getCode());
        map.put("name", p.getName());
        map.put("type", p.getType());
        map.put("parentId", p.getParentId());
        map.put("path", p.getPath());
        map.put("icon", p.getIcon());
        map.put("sort", p.getSort());
        return map;
    }
}
