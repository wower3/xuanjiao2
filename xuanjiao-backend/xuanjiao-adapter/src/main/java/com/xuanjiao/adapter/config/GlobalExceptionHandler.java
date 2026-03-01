package com.xuanjiao.adapter.config;

import com.xuanjiao.client.Result;
import com.xuanjiao.common.exception.BaseException;
import com.xuanjiao.common.exception.BusinessException;
import com.xuanjiao.common.exception.NotFoundException;
import com.xuanjiao.common.exception.PermissionException;
import com.xuanjiao.common.exception.SystemException;
import com.xuanjiao.common.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * <p>统一处理控制器层抛出的异常，返回标准化的错误响应。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>业务异常处理：捕获业务逻辑校验失败的异常</li>
 *   <li>权限异常处理：捕获权限不足的异常</li>
 *   <li>资源未找到异常处理：捕获资源不存在的异常</li>
 *   <li>参数验证异常处理：捕获参数验证失败的异常</li>
 *   <li>系统异常处理：捕获系统级错误</li>
 *   <li>通用异常处理：捕获未被其他处理器处理的异常</li>
 *   <li>日志记录：记录异常堆栈信息，便于问题排查</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 日志记录器
     *
     * <p>用于记录异常信息，便于开发人员排查问题。</p>
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     *
     * <p>捕获业务逻辑校验失败的异常，返回具体的业务错误信息。
     * 此类异常通常由业务规则验证失败引起。</p>
     *
     * @param e 业务异常实例
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理权限异常
     *
     * <p>捕获权限不足的异常，返回权限错误信息。
     * 此类异常由权限验证失败引起。</p>
     *
     * @param e 权限异常实例
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(PermissionException.class)
    public Result<Void> handlePermissionException(PermissionException e) {
        log.warn("权限异常: {}", e.getMessage());
        return Result.error(403, e.getMessage());
    }

    /**
     * 处理资源未找到异常
     *
     * <p>捕获资源不存在的异常，返回资源未找到错误信息。
     * 此类异常由查询不到对应资源引起。</p>
     *
     * @param e 资源未找到异常实例
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(NotFoundException.class)
    public Result<Void> handleNotFoundException(NotFoundException e) {
        log.warn("资源未找到异常: {}", e.getMessage());
        return Result.error(404, e.getMessage());
    }

    /**
     * 处理参数验证异常
     *
     * <p>捕获参数验证失败的异常，返回参数错误信息。
     * 此类异常由参数验证失败引起。</p>
     *
     * @param e 参数验证异常实例
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(ValidationException.class)
    public Result<Void> handleValidationException(ValidationException e) {
        log.warn("参数验证异常: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理系统异常
     *
     * <p>捕获系统级错误，返回系统错误信息。
     * 此类异常通常需要记录日志并进行告警。</p>
     *
     * @param e 系统异常实例
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(SystemException.class)
    public Result<Void> handleSystemException(SystemException e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return Result.error(500, e.getMessage());
    }

    /**
     * 处理运行时异常
     *
     * <p>捕获所有 RuntimeException 及其子类异常（已被自定义异常处理的除外），
     * 返回异常中的具体错误信息。
     * 此类异常通常由业务逻辑校验失败或数据验证失败引起。</p>
     *
     * @param e 运行时异常实例
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        // 过滤已被自定义异常处理的异常
        if (e instanceof BaseException) {
            return handleBaseException((BaseException) e);
        }
        log.error("运行时异常", e);
        return Result.error(e.getMessage());
    }

    /**
     * 处理基础异常
     *
     * <p>处理所有 BaseException 的子类异常。</p>
     *
     * @param e 基础异常实例
     * @return 包含错误信息的响应结果
     */
    private Result<Void> handleBaseException(BaseException e) {
        if (e instanceof BusinessException) {
            return handleBusinessException((BusinessException) e);
        } else if (e instanceof PermissionException) {
            return handlePermissionException((PermissionException) e);
        } else if (e instanceof NotFoundException) {
            return handleNotFoundException((NotFoundException) e);
        } else if (e instanceof ValidationException) {
            return handleValidationException((ValidationException) e);
        } else if (e instanceof SystemException) {
            return handleSystemException((SystemException) e);
        } else {
            log.error("未知异常类型: {}", e.getClass().getName(), e);
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 处理通用异常
     *
     * <p>捕获所有未被其他处理器处理的异常，返回通用错误提示。
     * 此处理器作为兜底，防止异常信息直接暴露给前端。</p>
     *
     * @param e 异常实例
     * @return 包含通用错误信息的响应结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统异常，请稍后重试");
    }
}
