package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材数据对象
 * <p>对应数据库表 asset，存储素材的持久化数据</p>
 * <p>设计说明：使用软删除策略，通过deleted字段控制</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.asset.entity.Asset
 */
@Data
@TableName("asset")
public class AssetDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 素材名称 */
    private String name;

    /** 素材类型：IMAGE、VIDEO、DOCUMENT */
    private String type;

    /** 文件相对路径，相对于配置的上传目录 */
    private String filePath;

    /** 缩略图路径 */
    private String thumbnailPath;

    /** 文件大小，单位字节 */
    private Long fileSize;

    /** 文件MD5值，用于去重和完整性校验 */
    private String md5;

    /** 素材状态：DRAFT、PENDING、APPROVED、REJECTED、DELETED */
    private String status;

    /** 版权声明：PUBLIC、INTERNAL、RESTRICTED */
    private String copyright;

    /** 上传用户ID */
    private Long uploadUserId;

    /** 关联的素材录入申请ID */
    private Long applicationId;

    /** 版权文件路径 */
    private String copyrightFilePath;

    /** 版权声明文本 */
    private String copyrightText;

    /** 素材描述 */
    private String description;

    /** 发布渠道：WEB、APP、WECHAT、ALL */
    private String publishChannel;

    /** 删除审批通过时间，用于计算7天后彻底删除 */
    private LocalDateTime deletionApproveTime;

    /** 创建时间，自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标识：0-未删除、1-已删除，查询时自动过滤 */
    @TableLogic
    private Integer deleted;
}
