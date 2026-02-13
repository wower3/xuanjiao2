package com.xuanjiao.client.dto.dept.dto;

import lombok.Data;
import java.util.List;

/**
 * 部门数据传输对象
 *
 * <p>用于在前后端之间传输部门信息，支持树形结构展示。
 * 包含部门基本信息、层级关系和子部门列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class DeptDTO {

    /**
     * 部门ID
     */
    private Long id;

    /**
     * 部门编码
     */
    private String code;

    /**
     * 部门全编码（包含所有上级编码）
     */
    private String fullCode;

    /**
     * 部门层级（从1开始）
     */
    private Integer level;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 父部门ID（顶级部门为null）
     */
    private Long parentId;

    /**
     * 部门负责人ID
     */
    private Long leaderId;

    /**
     * 排序号（同级部门排序）
     */
    private Integer sort;

    /**
     * 部门状态（1-启用、0-禁用）
     */
    private Integer status;

    /**
     * 子部门列表（用于构建部门树）
     */
    private List<DeptDTO> children;
}
