package com.xuanjiao.client.dto.deletion;

import com.xuanjiao.client.dto.common.BasePageQry;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取我的删除申请列表查询对象
 *
 * <p>用于查询当前用户发起的素材删除申请列表，支持按标题和状态筛选。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeletionGetMyApplicationsQry extends BasePageQry {

    /**
     * 申请标题（模糊查询）
     */
    private String title;

    /**
     * 申请状态（DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已驳回）
     */
    private String status;
}
