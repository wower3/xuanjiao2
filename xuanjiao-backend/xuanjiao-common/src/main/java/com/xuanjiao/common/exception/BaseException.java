package com.xuanjiao.common.exception;

/**
 * 基础异常类
 * <p>
 * 所有自定义异常的父类，继承 RuntimeException 以支持事务回滚
 * 提供 errorCode 和 message 两个属性
 *
 * @author xuanjiao
 * @date 2025-02-28
 */
public class BaseException extends RuntimeException {

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 构造函数
     *
     * @param message 错误信息
     */
    public BaseException(String message) {
        super(message);
        this.errorCode = getDefaultErrorCode();
    }

    /**
     * 构造函数
     *
     * @param message 错误信息
     * @param cause   原始异常
     */
    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = getDefaultErrorCode();
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @param cause     原始异常
     */
    public BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取默认错误码
     * 子类可以重写此方法提供默认错误码
     *
     * @return 默认错误码
     */
    protected String getDefaultErrorCode() {
        return "DEFAULT_ERROR";
    }
}
