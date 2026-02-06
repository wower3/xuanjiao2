package com.xuanjiao.domain.asset.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 素材实体
 * <p>代表系统中的媒体资产文件，包括视频、图片、文档等</p>
 * <p>具有状态流转特性：DRAFT → PENDING → APPROVED/REJECTED → DELETED</p>
 * <p>主要业务流程：素材录入申请 → 审批 → 素材使用/删除申请 → 审批</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.AssetDO
 */
@Data
public class Asset {
    /** 素材唯一标识，自增主键 */
    private Long id;

    /** 素材名称，用户上传时指定，最大100字符 */
    private String name;

    /** 素材类型：IMAGE-图片、VIDEO-视频、DOCUMENT-文档 */
    private String type;

    /** 文件相对路径，相对于配置的上传目录 */
    private String filePath;

    /** 缩略图路径，用于列表展示预览 */
    private String thumbnailPath;

    /** 文件大小，单位字节 */
    private Long fileSize;

    /** 文件MD5值，用于去重和完整性校验 */
    private String md5;

    /** 素材状态：DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝、DELETED-已删除 */
    private String status;

    /** 版权声明：PUBLIC-公开、INTERNAL-内部、RESTRICTED-受限 */
    private String copyright;

    /** 上传用户ID，关联sys_user表 */
    private Long uploadUserId;

    /** 关联的素材录入申请ID，素材通过哪个申请录入系统 */
    private Long applicationId;

    /** 版权文件路径，上传版权证明文件的存储路径 */
    private String copyrightFilePath;

    /** 版权声明文本，手动输入的版权信息 */
    private String copyrightText;

    /** 素材描述，说明素材的用途、来源等信息 */
    private String description;

    /** 发布渠道：WEB-网站、APP-客户端、WECHAT-微信、ALL-全渠道 */
    private String publishChannel;

    /** 标签ID列表，关联sys_tag表 */
    private List<Long> tagIds;

    /** 逻辑删除标识：0-未删除、1-已删除（用于软删除过滤） */
    private Integer deleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
