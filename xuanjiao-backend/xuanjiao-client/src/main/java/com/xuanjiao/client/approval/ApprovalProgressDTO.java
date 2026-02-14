package com.xuanjiao.client.approval;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批进度数据传输对象
 *
 * <p>用于在前后端之间传输审批进度信息，展示每个审批阶段的
 * 当前状态和各审批人的处理情况，支持子流程进度展示。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApprovalProgressDTO {

    /**
     * 进度记录ID
     */
    private Long id;

    /**
     * 审批实例ID
     */
    private Long instanceId;

    /**
     * 阶段ID
     */
    private Long stageId;

    /**
     * 阶段名称
     */
    private String stageName;

    /**
     * 阶段顺序（从1开始）
     */
    private Integer stageOrder;

    /**
     * 阶段状态
     * <ul>
     *   <li>PENDING - 待审批</li>
     *   <li>APPROVED - 已通过</li>
     *   <li>REJECTED - 已驳回</li>
     *   <li>SKIPPED - 已跳过</li>
     * </ul>
     */
    private String status;

    /**
     * 审批人列表
     */
    private List<ApproverInfo> approvers;

    /**
     * 是否是子流程（0-否，1-是）
     */
    private Integer isSubWorkflow;

    /**
     * 父实例ID（用于子流程）
     */
    private Long parentInstanceId;

    /**
     * 父任务ID（用于子流程，记录是哪个任务触发的子流程）
     */
    private Long parentTaskId;

    /**
     * 审批通过时间
     */
    private LocalDateTime approveTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 审批人信息
     *
     * <p>内部类，用于展示单个审批人的处理状态</p>
     *
     * @author xuanjiao
     * @since 1.0.0
     */
    @Data
    public static class ApproverInfo {

        /**
         * 审批人ID
         */
        private Long id;

        /**
         * 审批人姓名
         */
        private String name;

        /**
         * 审批状态（APPROVED-已通过，PENDING-待审批）
         */
        private String status;

        /**
         * 审批时间（字符串格式）
         */
        private String approveTime;

        /**
         * 审批意见
         */
        private String comment;
    }
}
