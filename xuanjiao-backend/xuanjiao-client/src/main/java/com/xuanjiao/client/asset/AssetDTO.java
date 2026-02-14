package com.xuanjiao.client.asset;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 素材资产数据传输对象
 *
 * <p>用于在前后端之间传输素材资产信息，包括文件基本信息、
 * 版权信息、发布渠道、标签等完整属性。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class AssetDTO {

    /**
     * 素材ID
     */
    private Long id;

    /**
     * 素材名称
     */
    private String name;

    /**
     * 素材类型（IMAGE-图片、VIDEO-视频、DOCUMENT-文档）
     */
    private String type;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 缩略图路径
     */
    private String thumbnailPath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件MD5校验值（用于去重）
     */
    private String md5;

    /**
     * 素材状态（DRAFT-草稿、PENDING-待审批、APPROVED-已通过、DELETED-已删除）
     */
    private String status;

    /**
     * 版权信息
     */
    private String copyright;

    /**
     * 上传用户ID
     */
    private Long uploadUserId;

    /**
     * 上传用户名称
     */
    private String uploadUserName;

    /**
     * 关联的素材录入申请ID
     */
    private Long applicationId;

    /**
     * 版权文件路径
     */
    private String copyrightFilePath;

    /**
     * 版权说明文字
     */
    private String copyrightText;

    /**
     * 素材描述
     */
    private String description;

    /**
     * 发布渠道
     */
    private String publishChannel;

    /**
     * 素材关联的标签列表
     */
    private List<TagDTO> tags;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否有下载权限
     *
     * <p>根据用户是否有所需素材的使用申请（APPROVED状态）来判断</p>
     */
    private Boolean canDownload;
}
