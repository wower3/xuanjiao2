package com.xuanjiao.client.dto.usage;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取使用申请详情查询对象
 *
 * <p>用于根据申请单ID获取素材使用申请的详细信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UsageApplyGetDetailQry {

    /**
     * 申请单ID
     */
    @NotNull(message = "申请单ID不能为空")
    private Long id;
}
