package com.xuanjiao.client.dto.common;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 分页查询基类
 * <p>所有分页查询 Qry 类的基类，提供统一的分页参数定义</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public abstract class BasePageQry {

    /**
     * 当前页码（从1开始，默认为1）
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页记录数（默认为10，最大为100）
     */
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer pageSize = 10;
}
