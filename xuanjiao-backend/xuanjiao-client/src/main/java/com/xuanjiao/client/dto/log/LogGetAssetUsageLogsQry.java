package com.xuanjiao.client.dto.log;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 获取素材使用记录查询对象
 */
@Data
public class LogGetAssetUsageLogsQry {

    @NotNull(message = "素材ID不能为空")
    private Long assetId;

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;
}
