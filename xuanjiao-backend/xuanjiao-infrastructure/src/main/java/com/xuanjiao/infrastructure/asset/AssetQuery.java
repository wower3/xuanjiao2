package com.xuanjiao.infrastructure.asset;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 素材查询条件对象
 *
 * <p>用于动态构建素材查询条件，支持多种查询方式。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class AssetQuery {

    /**
     * 素材ID
     */
    private Long id;

    /**
     * 素材名称（模糊查询）
     */
    private String name;

    /**
     * 素材类型（IMAGE, VIDEO, DOCUMENT）
     */
    private String type;

    /**
     * 素材状态（PENDING, APPROVED, REJECTED, DELETED, DRAFT）
     */
    private String status;

    /**
     * 素材状态列表（IN查询）
     */
    private List<String> statusList;

    /**
     * MD5值（用于去重校验）
     */
    private String md5;

    /**
     * 关联的申请ID
     */
    private Long applicationId;

    /**
     * 上传用户ID
     */
    private Long uploadUserId;

    /**
     * 删除审批时间早于指定时间（用于定时清理任务）
     */
    private LocalDateTime deletionApproveTimeBefore;

    /**
     * 删除标记（0-未删除、1-已删除）
     */
    private Integer deleted;

    /**
     * 排序字段
     */
    private String orderByField;

    /**
     * 排序方向（ASC/DESC）
     */
    private String orderByDirection;

    /**
     * 分页偏移量
     */
    private Integer offset;

    /**
     * 分页大小
     */
    private Integer limit;
}
