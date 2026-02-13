package com.xuanjiao.infrastructure.dataobject;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 素材录入申请数据对象（包含用户和部门信息）
 * 用于 JOIN 查询结果映射，避免 N+1 问题
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MaterialApplicationWithDetailsDO {
    // 素材录入申请字段
    private Long id;
    private String title;
    private Long applicantId;
    private Long maintainerId;
    private Long deptId;
    private Integer guaranteeDeclaration;
    private String status;
    private Long workflowId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联的申请人字段
    private String applicantName;

    // 关联的维护人字段
    private String maintainerName;

    // 关联的部门字段
    private String deptName;
}
