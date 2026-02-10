package com.xuanjiao.client.dto.dept;

import lombok.Data;

/**
 * 更新部门命令对象
 *
 * <p>封装更新部门所需的参数信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class DeptUpdateCmd {

    /**
     * 部门ID
     */
    private Long id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门编码
     */
    private String code;

    /**
     * 父部门ID
     */
    private Long parentId;

    /**
     * 排序号
     */
    private Integer sort;
}
