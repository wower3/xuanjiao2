package com.xuanjiao.client.dto.usage.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材使用日志数据传输对象
 *
 * <p>用于在前后端之间传输素材使用日志信息，记录素材的下载和使用行为。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UsageLogDTO {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 素材ID
     */
    private Long assetId;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作类型（DOWNLOAD-下载、VIEW-查看等）
     */
    private String action;

    /**
     * 操作IP地址
     */
    private String ip;

    /**
     * 用户所属部门名称
     */
    private String deptName;

    /**
     * 使用说明
     */
    private String usageDescription;

    /**
     * 使用发布渠道
     */
    private String usagePublishChannel;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
