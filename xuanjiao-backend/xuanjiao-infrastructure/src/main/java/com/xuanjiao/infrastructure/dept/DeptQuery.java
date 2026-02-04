package com.xuanjiao.infrastructure.dept;

import lombok.Data;
import java.util.List;

/**
 * 部门查询条件对象
 * 用于动态构建查询条件
 */
@Data
public class DeptQuery {

    /** 主键ID */
    private Long id;

    /** 部门编码 */
    private String code;

    /** 部门级别 */
    private Integer level;

    /** 部门名称（模糊查询） */
    private String nameKeyword;

    /** 父部门ID */
    private Long parentId;

    /** 负责人ID */
    private Long leaderId;

    /** 部门状态（0:禁用, 1:启用） */
    private Integer status;

    /** 删除标记（0:未删除, 1:已删除） */
    private Integer deleted;

    /** 排序字段 */
    private String orderByField;

    /** 排序方向（ASC/DESC） */
    private String orderByDirection;
}
