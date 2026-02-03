package com.xuanjiao.client.dto.approval;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 获取我发起的审批查询对象
 */
@Data
public class ApprovalGetMyAppliedQry {

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;

    private String businessType;

    private Boolean forAllUsers = false;

    private Long applicantId;

    private Long deptId;

    private String roleType;

    private String status;
}
