package com.xuanjiao.client.dto.material;

import com.xuanjiao.client.dto.common.BasePageQry;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取素材申请单草稿箱查询对象
 *
 * <p>用于查询当前用户保存的素材录入草稿列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialApplicationGetDraftsQry extends BasePageQry {
}
