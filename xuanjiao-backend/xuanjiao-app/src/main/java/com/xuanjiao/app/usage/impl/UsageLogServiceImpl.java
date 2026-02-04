package com.xuanjiao.app.usage.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.app.usage.UsageLogService;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.UsageLogDTO;
import com.xuanjiao.infrastructure.dataobject.UsageLogDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.usage.UsageLogMapper;
import com.xuanjiao.infrastructure.usage.UsageLogQuery;
import com.xuanjiao.infrastructure.user.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsageLogServiceImpl implements UsageLogService {

    @Resource
    private UsageLogMapper logMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public void log(Long assetId, Long userId, String action, String ip) {
        UsageLogDO log = new UsageLogDO();
        log.setAssetId(assetId);
        log.setUserId(userId);
        log.setAction(action);
        log.setIp(ip);
        logMapper.insert(log);
    }

    @Override
    public void logDownload(Long assetId, Long userId, String ip, String deptName, String usageDescription, String usagePublishChannel) {
        UsageLogDO log = new UsageLogDO();
        log.setAssetId(assetId);
        log.setUserId(userId);
        log.setAction("DOWNLOAD");
        log.setIp(ip);
        log.setDeptName(deptName);
        log.setUsageDescription(usageDescription);
        log.setUsagePublishChannel(usagePublishChannel);
        logMapper.insert(log);
    }

    @Override
    public PageResult<Map<String, Object>> query(String action, int pageNum, int pageSize) {
        UsageLogQuery query = new UsageLogQuery();
        if (StringUtils.hasText(action)) {
            query.setAction(action);
        }
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        Page<UsageLogDO> page = logMapper.selectPage(new Page<>(pageNum, pageSize), query);
        List<Map<String, Object>> list = page.getRecords().stream().map(this::toMap).collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<UsageLogDTO> getAssetUsageLogs(Long assetId, int pageNum, int pageSize) {
        UsageLogQuery query = new UsageLogQuery();
        query.setAssetId(assetId);
        query.setAction("DOWNLOAD");
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        Page<UsageLogDO> page = logMapper.selectPage(new Page<>(pageNum, pageSize), query);
        List<UsageLogDTO> list = page.getRecords().stream().map(this::toDTO).collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    private Map<String, Object> toMap(UsageLogDO log) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("assetId", log.getAssetId());
        map.put("userId", log.getUserId());
        map.put("action", log.getAction());
        map.put("ip", log.getIp());
        map.put("createTime", log.getCreateTime());
        return map;
    }

    private UsageLogDTO toDTO(UsageLogDO log) {
        UsageLogDTO dto = new UsageLogDTO();
        dto.setId(log.getId());
        dto.setAssetId(log.getAssetId());
        dto.setUserId(log.getUserId());
        dto.setAction(log.getAction());
        dto.setIp(log.getIp());
        dto.setDeptName(log.getDeptName());
        dto.setUsageDescription(log.getUsageDescription());
        dto.setUsagePublishChannel(log.getUsagePublishChannel());
        dto.setCreateTime(log.getCreateTime());

        // 填充用户名称
        if (log.getUserId() != null) {
            UserDO user = userMapper.selectById(log.getUserId());
            if (user != null) {
                dto.setUserName(user.getRealName());
            }
        }

        return dto;
    }
}
