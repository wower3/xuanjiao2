package com.xuanjiao.client.dto.log;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 查询日志查询对象
 */
@Data
public class LogQueryLogsQry {

    private String action;

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;
}
