package com.xuanjiao.infrastructure.dataobject;

import lombok.Data;
import java.util.Date;

/**
 * 用户详情数据对象（包含部门和角色信息）
 * 用于 JOIN 查询结果映射，避免 N+1 问题
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UserWithDeptRoleDO {
    // 用户基本信息
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private Integer status;
    private Date createTime;

    // 关联信息
    private Long roleId;
    private String roleName;
    private String roleType;
    private Long deptId;
    private String deptName;
}
