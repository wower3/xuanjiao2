package com.xuanjiao.client.dto;

import lombok.Data;
import java.time.LocalDateTime;

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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
