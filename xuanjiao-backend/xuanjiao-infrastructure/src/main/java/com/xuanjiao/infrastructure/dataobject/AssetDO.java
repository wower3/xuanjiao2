package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材数据对象
 *
 * <p>映射数据库 asset 表，用于 MyBatis 数据访问。</p>
 * <p>存储素材的基本信息，包括文件路径、状态、版权信息等。</p>
 * <p>使用软删除策略，通过 deleted 字段控制数据的可见性。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("asset")
public class AssetDO {

    /**
     * 素材ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 素材名称
     */
    private String name;

    /**
     * 素材类型：IMAGE-图片、VIDEO-视频、DOCUMENT-文档
     */
    private String type;

    /**
     * 文件相对路径，相对于配置的上传目录
     */
    private String filePath;

    /**
     * 缩略图路径
     */
    private String thumbnailPath;

    /**
     * 文件大小，单位字节
     */
    private Long fileSize;

    /**
     * 文件MD5值，用于去重和完整性校验
     */
    private String md5;

    /**
     * 素材状态：DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝、DELETED-已删除
     */
    private String status;

    /**
     * 版权声明：PUBLIC-公开、INTERNAL-内部、RESTRICTED-受限
     */
    private String copyright;

    /**
     * 上传用户ID
     */
    private Long uploadUserId;

    /**
     * 关联的素材录入申请ID
     */
    private Long applicationId;

    /**
     * 版权文件路径
     */
    private String copyrightFilePath;

    /**
     * 版权声明文本
     */
    private String copyrightText;

    /**
     * 素材描述
     */
    private String description;

    /**
     * 发布渠道：WEB-网页、APP-应用、WECHAT-微信、ALL-全部
     */
    private String publishChannel;

    /**
     * 删除审批通过时间，用于计算7天后彻底删除
     */
    private LocalDateTime deletionApproveTime;

    /**
     * 创建时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识：0-未删除、1-已删除，查询时自动过滤
     */
    @TableLogic
    private Integer deleted;
}
