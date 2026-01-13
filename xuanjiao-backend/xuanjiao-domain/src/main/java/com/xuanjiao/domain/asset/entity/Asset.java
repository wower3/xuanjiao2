package com.xuanjiao.domain.asset.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Asset {
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
    private Long applicationId;
    private String copyrightFilePath;
    private String copyrightText;
    private String description;
    private String publishChannel;
    private List<Long> tagIds;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
