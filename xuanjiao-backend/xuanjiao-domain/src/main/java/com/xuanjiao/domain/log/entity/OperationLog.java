package com.xuanjiao.domain.log.entity;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 * <p>记录用户在系统中的关键操作，用于审计和追溯</p>
 * <p>包括操作人、操作类型、操作目标、操作详情等信息</p>
 * <p>是系统安全审计的重要组成部分</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.OperationLogDO
 */
public class OperationLog {
    /** 日志唯一标识，自增主键 */
    private Long id;

    /** 操作人ID，关联sys_user表 */
    private Long operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** 操作类型：CREATE-创建、UPDATE-更新、DELETE-删除、APPROVE-审批、LOGIN-登录、LOGOUT-登出等 */
    private String operationType;

    /** 操作目标类型：如ASSET-素材、USAGE_APPLY-使用申请等 */
    private String targetType;

    /** 操作目标ID */
    private Long targetId;

    /** 操作目标名称 */
    private String targetName;

    /** 操作详情，JSON格式，记录操作的具体内容 */
    private String operationDetail;

    /** IP地址，操作发生的客户端IP */
    private String ipAddress;

    /** 创建时间 */
    private LocalDateTime createTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getOperationDetail() {
        return operationDetail;
    }

    public void setOperationDetail(String operationDetail) {
        this.operationDetail = operationDetail;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
