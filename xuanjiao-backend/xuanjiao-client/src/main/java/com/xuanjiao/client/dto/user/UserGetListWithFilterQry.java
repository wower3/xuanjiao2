package com.xuanjiao.client.dto.user;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
public class UserGetListWithFilterQry {

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

    /**
     * 当前页码（从1开始，默认为1）
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页记录数（默认为20，最大为100）
     */
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer pageSize = 20;
}
