package com.xuanjiao.infrastructure.usage;

import lombok.Data;

import java.util.List;

/**
 * 素材使用申请查询条件对象
 * 用于动态构建查询条件
 */
@Data
public class UsageApplyQuery {

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 申请单标题（模糊查询） */
    private String titleKeyword;

    /** 申请状态 */
    private String status;

    /** 状态列表（IN查询） */
    private List<String> statusList;

    /** 是否草稿：0-已提交, 1-草稿 */
    private Integer draft;

    /** 部门ID */
    private Long deptId;

    /** 删除标记（0:未删除, 1:已删除） */
    private Integer deleted;

    /** 排序字段 */
    private String orderByField;

    /** 排序方向 */
    private String orderByDirection;

    /** 分页偏移量 */
    private Integer offset;

    /** 分页限制 */
    private Integer limit;
}
