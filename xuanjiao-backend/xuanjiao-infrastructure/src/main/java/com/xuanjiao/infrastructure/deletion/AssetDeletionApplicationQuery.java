package com.xuanjiao.infrastructure.deletion;

import lombok.Data;

import java.util.List;

/**
 * AssetDeletionApplication Query Object
 * Dynamic query parameters for AssetDeletionApplicationMapper
 */
@Data
public class AssetDeletionApplicationQuery {
    private Long id;
    private String title;
    private Long applicantId;
    private Long deptId;
    private Long workflowId;
    private String status;
    private Integer deleted;
    private List<Long> applicantIds; // IN查询
    private List<String> statusIn; // IN查询
    private String orderByField;
    private String orderByDirection;
}
