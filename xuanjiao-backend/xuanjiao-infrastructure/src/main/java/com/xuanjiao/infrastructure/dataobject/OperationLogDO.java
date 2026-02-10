package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 操作日志数据对象
 *
 * <p>映射数据库 operation_log 表，用于 MyBatis 数据访问。</p>
 * <p>记录用户的关键操作行为，支持审计和追溯。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@TableName("operation_log")
public class OperationLogDO {

    /**
     * 日志ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作类型：CREATE-创建、UPDATE-更新、DELETE-删除等
     */
    private String operationType;

    /**
     * 目标类型：USER-用户、ASSET-素材、WORKFLOW-工作流等
     */
    private String targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 目标名称
     */
    private String targetName;

    /**
     * 操作详情
     */
    private String operationDetail;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 创建时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT)
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
     * 获取操作人姓名
     *
     * @return 操作人姓名
     */
    public String getOperatorName() {
        return operatorName;
    }

    /**
     * 设置操作人姓名
     *
     * @param operatorName 操作人姓名
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
     * 获取目标类型
     *
     * @return 目标类型
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * 设置目标类型
     *
     * @param targetType 目标类型
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    /**
     * 获取目标ID
     *
     * @return 目标ID
     */
    public Long getTargetId() {
        return targetId;
    }

    /**
     * 设置目标ID
     *
     * @param targetId 目标ID
     */
    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    /**
     * 获取目标名称
     *
     * @return 目标名称
     */
    public String getTargetName() {
        return targetName;
    }

    /**
     * 设置目标名称
     *
     * @param targetName 目标名称
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
