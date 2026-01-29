package com.xuanjiao.client.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TagDTO {
    private Long id;
    private String name;
    private String category;
    private LocalDateTime createTime;
}
