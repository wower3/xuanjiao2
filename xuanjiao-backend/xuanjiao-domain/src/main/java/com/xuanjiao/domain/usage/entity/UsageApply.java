package com.xuanjiao.domain.usage.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UsageApply {
    private Long id;
    private Long userId;
    private String title;          // 申请单标题
    private String purpose;        // 申请说明
    private String scope;          // 使用范围
    private Long workflowId;
    private String status;
    private Long approvalInstanceId;

    // 新增字段
    private String attachmentPath;       // 附件路径
    private Integer isSecondaryCreation; // 是否二次创作:0-否,1-是
    private String publishChannel;       // 发布渠道
    private Long deptId;                 // 申请部门ID
    private Integer draft;               // 是否草稿:0-已提交,1-草稿

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联的素材列表（非数据库字段，用于业务逻辑）
    private List<UsageApplyAsset> assets;
}
