package com.xuanjiao.domain.log.entity;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 *
 * <p>记录用户在系统中的关键操作，用于审计和追溯。</p>
 * <p>包括操作人、操作类型、操作目标、操作详情等信息。</p>
 * <p>是系统安全审计的重要组成部分。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public class OperationLog {

    /**
     * 日志唯一标识
     *
     * <p>自增主键。</p>
     */
    private Long id;

    /**
     * 操作人ID
     *
     * <p>关联sys_user表。</p>
     */
    private Long operatorId;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 操作类型
     *
     * <p>CREATE-创建、UPDATE-更新、DELETE-删除、APPROVE-审批、LOGIN-登录、LOGOUT-登出等。</p>
     */
    private String operationType;

    /**
     * 操作目标类型
     *
     * <p>如ASSET-素材、USAGE_APPLY-使用申请等。</p>
     */
    private String targetType;

    /**
     * 操作目标ID
     */
    private Long targetId;

    /**
     * 操作目标名称
     */
    private String targetName;

    /**
     * 操作详情
     *
     * <p>JSON格式，记录操作的具体内容。</p>
     */
    private String operationDetail;

    /**
     * IP地址
     *
     * <p>操作发生的客户端IP。</p>
     */
    private String ipAddress;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 获取日志ID
     *
     * @return 日志ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置日志ID
     *
     * @param id 日志ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取操作人ID
     *
     * @return 操作人ID
     */
    public Long getOperatorId() {
        return operatorId;
    }

    /**
     * 设置操作人ID
     *
     * @param operatorId 操作人ID
     */
    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    /**
     * 获取操作人名称
     *
     * @return 操作人名称
     */
    public String getOperatorName() {
        return operatorName;
    }

    /**
     * 设置操作人名称
     *
     * @param operatorName 操作人名称
     */
    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    /**
     * 获取操作类型
     *
     * @return 操作类型
     */
    public String getOperationType() {
        return operationType;
    }

    /**
     * 设置操作类型
     *
     * @param operationType 操作类型
     */
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    /**
     * 获取操作目标类型
     *
     * @return 操作目标类型
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * 设置操作目标类型
     *
     * @param targetType 操作目标类型
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    /**
     * 获取操作目标ID
     *
     * @return 操作目标ID
     */
    public Long getTargetId() {
        return targetId;
    }

    /**
     * 设置操作目标ID
     *
     * @param targetId 操作目标ID
     */
    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    /**
     * 获取操作目标名称
     *
     * @return 操作目标名称
     */
    public String getTargetName() {
        return targetName;
    }

    /**
     * 设置操作目标名称
     *
     * @param targetName 操作目标名称
     */
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    /**
     * 获取操作详情
     *
     * @return 操作详情
     */
    public String getOperationDetail() {
        return operationDetail;
    }

    /**
     * 设置操作详情
     *
     * @param operationDetail 操作详情
     */
    public void setOperationDetail(String operationDetail) {
        this.operationDetail = operationDetail;
    }

    /**
     * 获取IP地址
     *
     * @return IP地址
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * 设置IP地址
     *
     * @param ipAddress IP地址
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
