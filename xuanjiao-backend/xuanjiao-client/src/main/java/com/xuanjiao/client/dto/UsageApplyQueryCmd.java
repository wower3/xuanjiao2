package com.xuanjiao.client.dto;

import lombok.Data;

@Data
public class UsageApplyQueryCmd {
    private Long assetId;
    private String status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
