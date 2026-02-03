package com.xuanjiao.client.dto.task;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 查询草稿箱查询对象
 */
@Data
public class TaskQueryDraftsQry {

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;

    private String draftType;

    private String title;
}
