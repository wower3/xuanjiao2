package com.xuanjiao.app.log;

import com.xuanjiao.domain.log.entity.OperationLog;

/**
 * 操作日志服务接口
 *
 * <p>提供操作日志的记录功能。记录用户在系统中的关键操作行为，
 * 用于审计和问题追溯。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>记录用户操作日志</li>
 *   <li>支持多种操作类型和目标类型</li>
 * </ul>
 *
 * <p>常见操作类型：</p>
 * <ul>
 *   <li>CREATE - 创建</li>
 *   <li>UPDATE - 更新</li>
 *   <li>DELETE - 删除</li>
 *   <li>APPROVE - 审批通过</li>
 *   <li>REJECT - 审批驳回</li>
 *   <li>DOWNLOAD - 下载</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.log.impl.OperationLogServiceImpl
 */
public interface OperationLogService {

    /**
     * 记录操作日志
     *
     * <p>记录用户在系统中执行的关键操作。日志信息包括操作人、
     * 操作类型、目标对象、操作详情等。</p>
     *
     * @param operatorId 操作人ID
     * @param operatorName 操作人名称
     * @param operationType 操作类型（如：CREATE, UPDATE, DELETE, APPROVE, REJECT, DOWNLOAD）
     * @param targetType 目标类型（如：ASSET, USER, WORKFLOW, APPLICATION）
     * @param targetId 目标ID
     * @param targetName 目标名称（用于日志展示）
     * @param detail 操作详情（JSON格式或文本描述）
     * @param ipAddress 操作者IP地址
     */
    void log(Long operatorId, String operatorName, String operationType,
            String targetType, Long targetId, String targetName, String detail, String ipAddress);
}
