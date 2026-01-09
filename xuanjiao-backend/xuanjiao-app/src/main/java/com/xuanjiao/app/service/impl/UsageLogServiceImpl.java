package com.xuanjiao.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.app.service.UsageLogService;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.infrastructure.dataobject.UsageLogDO;
import com.xuanjiao.infrastructure.mapper.UsageLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsageLogServiceImpl implements UsageLogService {

    @Resource
    private UsageLogMapper logMapper;

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
    public PageResult<Map<String, Object>> query(String action, int pageNum, int pageSize) {
        LambdaQueryWrapper<UsageLogDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(action)) {
            wrapper.eq(UsageLogDO::getAction, action);
        }
        wrapper.orderByDesc(UsageLogDO::getCreateTime);
        Page<UsageLogDO> page = logMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Map<String, Object>> list = page.getRecords().stream().map(this::toMap).collect(Collectors.toList());
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
}
