package com.xuanjiao.infrastructure.dataobject;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 使用日志数据对象（包含用户信息）
 * 用于 JOIN 查询结果映射，避免 N+1 问题
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UsageLogWithUserDO {
    // 使用日志字段
    private Long id;
    private Long assetId;
    private Long userId;
    private String action;
    private String ip;
    private String deptName;
    private String usageDescription;
    private String usagePublishChannel;
    private LocalDateTime createTime;

    // 关联的用户字段
    private String username;
    private String realName;
}
