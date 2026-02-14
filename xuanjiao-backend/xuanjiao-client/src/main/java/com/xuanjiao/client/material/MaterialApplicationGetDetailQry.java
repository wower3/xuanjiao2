package com.xuanjiao.client.material;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取素材申请单详情查询对象
 *
 * <p>用于根据申请单ID获取素材录入申请的详细信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MaterialApplicationGetDetailQry {

    /**
     * 申请单ID
     */
    @NotNull(message = "申请单ID不能为空")
    private Long id;
}
