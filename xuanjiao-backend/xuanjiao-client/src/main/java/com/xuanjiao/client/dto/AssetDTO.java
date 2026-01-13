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
}
