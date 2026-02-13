package com.xuanjiao.client.dto.material;

import com.xuanjiao.client.dto.common.BasePageQry;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取我的素材申请单查询对象
 *
 * <p>用于查询当前用户发起的素材录入申请列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialApplicationGetMyApplicationsQry extends BasePageQry {
}
