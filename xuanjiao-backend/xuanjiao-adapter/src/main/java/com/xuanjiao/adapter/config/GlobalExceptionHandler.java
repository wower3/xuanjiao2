package com.xuanjiao.adapter.config;

import com.xuanjiao.client.Result;
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
 *   <li>运行时异常处理：捕获 RuntimeException，返回具体错误信息</li>
 *   <li>通用异常处理：捕获 Exception，返回通用错误提示</li>
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
     * 处理运行时异常
     *
     * <p>捕获所有 RuntimeException 及其子类异常，返回异常中的具体错误信息。
     * 此类异常通常由业务逻辑校验失败或数据验证失败引起。</p>
     *
     * @param e 运行时异常实例
     * @return 包含错误信息的响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return Result.error(e.getMessage());
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
