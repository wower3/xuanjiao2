package com.xuanjiao.infrastructure.dataobject;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 使用申请数据对象（包含用户信息）
 * 用于 JOIN 查询结果映射，避免 N+1 问题
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UsageApplyWithUserDO {
    // 使用申请字段
    private Long id;
    private String title;
    private Long userId;
    private Long deptId;
    private Long workflowId;
    private String status;
    private Integer draft;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联的用户字段
    private String username;
    private String realName;
    private String email;
    private String phone;
    private Integer userStatus;

    // 关联的部门字段
    private String deptName;
}
