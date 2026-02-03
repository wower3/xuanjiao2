package com.xuanjiao.client.dto.user;

import lombok.Data;

import java.util.List;

/**
 * 获取用户列表（带筛选）查询对象
 */
@Data
public class UserGetListWithFilterQry {

    private List<Long> roleIds;

    private Long deptId;

    private Boolean includeSubDept = true;
}
