package com.xuanjiao.domain.dept.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 部门实体
 *
 * <p>代表组织架构中的部门，支持多级部门结构。</p>
 * <p>通过parentId实现树形结构，通过fullCode实现快速层级查询。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class Dept {

    /**
     * 部门唯一标识
     *
     * <p>自增主键。</p>
     */
    private Long id;

    /**
     * 部门编码
     *
     * <p>唯一标识。</p>
     */
    private String code;

    /**
     * 部门层级
     *
     * <p>1-一级、2-二级、3-三级。</p>
     */
    private Integer level;

    /**
     * 完整部门编码
     *
     * <p>格式：父级编码.当前编码，用于快速查询和排序。</p>
     */
    private String fullCode;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 父部门ID
     *
     * <p>顶级部门为0。</p>
     */
    private Long parentId;

    /**
     * 部门负责人ID
     *
     * <p>关联sys_user表。</p>
     */
    private Long leaderId;

    /**
     * 排序序号
     *
     * <p>数值越小越靠前。</p>
     */
    private Integer sort;

    /**
     * 部门状态
     *
     * <p>1-正常、0-停用。</p>
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
