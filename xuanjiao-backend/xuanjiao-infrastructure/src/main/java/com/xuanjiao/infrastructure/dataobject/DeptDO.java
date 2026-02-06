package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 部门数据对象
 * <p>对应数据库表 sys_dept，存储部门的持久化数据</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.dept.entity.Dept
 */
@Data
@TableName("sys_dept")
public class DeptDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 部门编码，唯一标识 */
    private String code;

    /** 部门层级：1-一级、2-二级、3-三级 */
    private Integer level;

    /** 完整部门编码，格式：父级编码.当前编码 */
    private String fullCode;

    /** 部门名称 */
    private String name;

    /** 父部门ID，顶级部门为0 */
    private Long parentId;

    /** 部门负责人ID */
    private Long leaderId;

    /** 排序序号 */
    private Integer sort;

    /** 部门状态：1-正常、0-停用 */
    private Integer status;

    /** 创建时间，自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标识：0-未删除、1-已删除 */
    @TableLogic
    private Integer deleted;
}
