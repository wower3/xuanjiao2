package com.xuanjiao.client.dto;

import lombok.Data;

@Data
public class AssetQueryCmd {
    private String name;
    private String type;
    private String status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
