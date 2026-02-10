package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材使用日志数据对象
 *
 * <p>映射数据库 usage_log 表，用于 MyBatis 数据访问。</p>
 * <p>记录素材的下载、使用等操作行为，用于审计追溯。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("usage_log")
public class UsageLogDO {

    /**
     * 日志ID（主键）
     */
    @TableId(type = IdType.AUTO)
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
     * 操作类型：DOWNLOAD-下载、VIEW-查看等
     */
    private String action;

    /**
     * 操作IP地址
     */
    private String ip;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 使用说明
     */
    private String usageDescription;

    /**
     * 发布渠道
     */
    private String usagePublishChannel;

    /**
     * 创建时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
