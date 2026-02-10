package com.xuanjiao.client.dto.deletion;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取删除申请详情查询对象
 *
 * <p>用于根据申请单ID获取素材删除申请的详细信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class DeletionGetDetailQry {

    /**
     * 申请单ID
     */
    @NotNull(message = "申请单ID不能为空")
    private Long id;
}
