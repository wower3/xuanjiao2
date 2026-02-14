package com.xuanjiao.client.log;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 获取素材使用记录查询对象
 *
 * <p>用于查询指定素材的使用记录列表，包括下载、查看等操作记录。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class LogGetAssetUsageLogsQry {

    /**
     * 素材ID
     */
    @NotNull(message = "素材ID不能为空")
    private Long assetId;

    /**
     * 当前页码（从1开始，默认为1）
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页记录数（默认为10）
     */
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;
}
