package com.xuanjiao.domain.dept.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 部门实体
 * <p>代表组织架构中的部门，支持多级部门结构</p>
 * <p>通过parentId实现树形结构，通过fullCode实现快速层级查询</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.DeptDO
 */
@Data
public class Dept {
    /** 部门唯一标识，自增主键 */
    private Long id;

    /** 部门编码，唯一标识 */
    private String code;

    /** 部门层级，1-一级、2-二级、3-三级 */
    private Integer level;

    /** 完整部门编码，格式：父级编码.当前编码，用于快速查询和排序 */
    private String fullCode;

    /** 部门名称 */
    private String name;

    /** 父部门ID，顶级部门为0 */
    private Long parentId;

    /** 部门负责人ID，关联sys_user表 */
    private Long leaderId;

    /** 排序序号，数值越小越靠前 */
    private Integer sort;

    /** 部门状态：1-正常、0-停用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
