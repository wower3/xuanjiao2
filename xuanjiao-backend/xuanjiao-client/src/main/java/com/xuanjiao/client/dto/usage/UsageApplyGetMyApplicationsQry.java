package com.xuanjiao.client.dto.usage;

import com.xuanjiao.client.dto.common.BasePageQry;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取我的使用申请查询对象
 *
 * <p>用于查询当前用户发起的素材使用申请列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UsageApplyGetMyApplicationsQry extends BasePageQry {
}
