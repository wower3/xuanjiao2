package com.xuanjiao.client.dto.deletion;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 获取我的删除申请列表查询对象
 */
@Data
public class DeletionGetMyApplicationsQry {

    private String title;

    private String status;

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;
}
