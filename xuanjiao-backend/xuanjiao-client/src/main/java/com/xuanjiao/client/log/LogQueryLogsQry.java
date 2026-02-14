package com.xuanjiao.client.log;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 查询日志查询对象
 *
 * <p>用于查询系统操作日志列表，支持按操作类型筛选。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class LogQueryLogsQry {

    /**
     * 操作类型（LOGIN-登录、LOGOUT-登出、DOWNLOAD-下载等）
     */
    private String action;

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
