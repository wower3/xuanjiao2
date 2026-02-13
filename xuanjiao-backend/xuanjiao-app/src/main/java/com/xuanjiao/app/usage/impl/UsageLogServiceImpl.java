package com.xuanjiao.app.usage.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.app.usage.UsageLogService;
import com.xuanjiao.client.dto.common.PageResult;
import com.xuanjiao.client.dto.usage.dto.UsageLogDTO;
import com.xuanjiao.infrastructure.dataobject.UsageLogDO;
import com.xuanjiao.infrastructure.dataobject.UsageLogWithUserDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.usage.UsageLogMapper;
import com.xuanjiao.infrastructure.usage.UsageLogQuery;
import com.xuanjiao.infrastructure.user.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 素材使用日志服务实现类
 * <p>实现UsageLogService接口，封装使用日志业务逻辑</p>
 * <p>核心功能：记录使用日志、下载日志、日志查询</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.usage.UsageLogService
 */
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
    public PageResult<UsageLogDTO> query(String action, int pageNum, int pageSize) {
        UsageLogQuery query = new UsageLogQuery();
        if (StringUtils.hasText(action)) {
            query.setAction(action);
        }
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        Page<UsageLogWithUserDO> page = new Page<>(pageNum, pageSize);
        IPage<UsageLogWithUserDO> pageResult = logMapper.selectPageWithUser(page, query);
        List<UsageLogDTO> list = pageResult.getRecords().stream().map(this::convertFromDetailDO).collect(Collectors.toList());
        return PageResult.of(list, pageResult.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<UsageLogDTO> getAssetUsageLogs(Long assetId, int pageNum, int pageSize) {
        UsageLogQuery query = new UsageLogQuery();
        query.setAssetId(assetId);
        query.setAction("DOWNLOAD");
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        Page<UsageLogWithUserDO> page = new Page<>(pageNum, pageSize);
        IPage<UsageLogWithUserDO> pageResult = logMapper.selectPageWithUser(page, query);
        List<UsageLogDTO> list = pageResult.getRecords().stream().map(this::convertFromDetailDO).collect(Collectors.toList());
        return PageResult.of(list, pageResult.getTotal(), pageNum, pageSize);
    }

    /**
     * 将 UsageLogWithUserDO 转换为 UsageLogDTO
     * 用于 JOIN 查询结果，避免 N+1 问题
     *
     * @param log 使用日志详情数据对象
     * @return 使用日志DTO
     */
    private UsageLogDTO convertFromDetailDO(UsageLogWithUserDO log) {
        if (log == null) return null;
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
        // 用户信息已经包含在 log 中
        dto.setUsername(log.getRealName());
        return dto;
    }

    /**
     * 保留旧方法用于兼容性（现在已不再使用）
     */
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
                dto.setUsername(user.getRealName());
            }
        }

        return dto;
    }
}
