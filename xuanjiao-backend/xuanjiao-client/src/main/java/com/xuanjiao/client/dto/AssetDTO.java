package com.xuanjiao.client.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssetDTO {
    private Long id;
    private String name;
    private String type;
    private String filePath;
    private String thumbnailPath;
    private Long fileSize;
    private String md5;
    private String status;
    private String copyright;
    private Long uploadUserId;
    private String uploadUserName;
    private Long applicationId;
    private String copyrightFilePath;
    private String copyrightText;
    private String description;
    private String publishChannel;
    private List<TagDTO> tags;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 是否有下载权限
     * 根据用户是否有所需素材的使用申请（APPROVED状态）来判断
     */
    private Boolean canDownload;
}
