package com.xuanjiao.client.dto.dept;

import lombok.Data;

/**
 * 更新部门命令对象
 */
@Data
public class DeptUpdateCmd {

    private Long id;

    private String name;

    private String code;

    private Long parentId;

    private Integer sort;
}
