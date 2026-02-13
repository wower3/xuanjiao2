package com.xuanjiao.client.dto.log;

import com.xuanjiao.client.dto.common.BasePageQry;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询日志查询对象
 *
 * <p>用于查询系统操作日志列表，支持按操作类型筛选。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogQueryLogsQry extends BasePageQry {

    /**
     * 操作类型（LOGIN-登录、LOGOUT-登出、DOWNLOAD-下载等）
     */
    private String action;
}
