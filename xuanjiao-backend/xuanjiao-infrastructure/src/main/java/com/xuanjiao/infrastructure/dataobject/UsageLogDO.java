package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材使用日志数据对象
 * <p>对应数据库表 usage_log，存储素材使用日志的持久化数据</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.log.entity.UsageLog
 */
@Data
@TableName("usage_log")
public class UsageLogDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assetId;
    private Long userId;
    private String action;
    private String ip;
    private String deptName;
    private String usageDescription;
    private String usagePublishChannel;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
