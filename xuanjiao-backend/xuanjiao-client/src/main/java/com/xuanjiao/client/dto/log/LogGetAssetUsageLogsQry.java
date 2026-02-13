package com.xuanjiao.client.dto.log;

import com.xuanjiao.client.dto.common.BasePageQry;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
@EqualsAndHashCode(callSuper = true)
public class LogGetAssetUsageLogsQry extends BasePageQry {

    /**
     * 素材ID
     */
    @NotNull(message = "素材ID不能为空")
    private Long assetId;
}
