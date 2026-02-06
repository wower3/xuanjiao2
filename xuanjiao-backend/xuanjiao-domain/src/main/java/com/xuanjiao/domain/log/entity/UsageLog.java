package com.xuanjiao.domain.log.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材使用日志实体
 * <p>记录用户对素材的使用行为，包括下载、使用申请等</p>
 * <p>用于统计素材的使用情况和分析素材价值</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.UsageLogDO
 */
@Data
public class UsageLog {
    /** 日志唯一标识，自增主键 */
    private Long id;

    /** 素材ID，关联Asset表 */
    private Long assetId;

    /** 使用人ID，关联sys_user表 */
    private Long userId;

    /** 操作动作：DOWNLOAD-下载、VIEW-查看、USE_APPLY-使用申请 */
    private String action;

    /** 操作详情 */
    private String detail;

    /** IP地址 */
    private String ip;

    /** 部门名称 */
    private String deptName;

    /** 使用说明 */
    private String usageDescription;

    /** 发布渠道 */
    private String usagePublishChannel;

    /** 创建时间 */
    private LocalDateTime createTime;
}
