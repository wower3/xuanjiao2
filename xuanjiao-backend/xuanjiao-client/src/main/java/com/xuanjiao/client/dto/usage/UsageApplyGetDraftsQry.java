package com.xuanjiao.client.dto.usage;

import com.xuanjiao.client.dto.common.BasePageQry;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取使用申请草稿箱查询对象
 *
 * <p>用于查询当前用户保存的素材使用草稿列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UsageApplyGetDraftsQry extends BasePageQry {
}
