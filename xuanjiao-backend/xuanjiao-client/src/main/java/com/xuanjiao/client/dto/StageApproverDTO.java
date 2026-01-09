package com.xuanjiao.client.dto;

import lombok.Data;

@Data
public class StageApproverDTO {
    private Long id;
    private String approverType;
    private Long approverId;
    private String approverName;
}
