package com.xuanjiao.client.dto.user;

import com.xuanjiao.client.dto.common.BasePageQry;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 获取用户列表（带筛选）查询对象
 *
 * <p>用于按条件筛选查询用户列表，支持按角色、部门、关键词筛选和分页。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserGetListWithFilterQry extends BasePageQry {

    /**
     * 角色ID列表筛选
     */
    private List<Long> roleIds;

    /**
     * 部门ID筛选
     */
    private Long deptId;

    /**
     * 是否包含子部门（默认为true）
     */
    private Boolean includeSubDept = true;

    /**
     * 关键词搜索（匹配姓名/用户名）
     */
    private String keyword;
}
