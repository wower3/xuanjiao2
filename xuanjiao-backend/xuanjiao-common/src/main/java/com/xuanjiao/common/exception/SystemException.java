package com.xuanjiao.common.exception;

/**
 * 系统异常
 * <p>
 * 用于系统级错误，如：
 * - 文件上传失败
 * - 对象转换失败
 * - 数据库操作失败
 *
 * 注意：此类异常通常需要记录日志并进行告警
 *
 * @author xuanjiao
 * @date 2025-02-28
 */
public class SystemException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "SYSTEM_ERROR";

    /**
     * 构造函数
     *
     * @param message 错误信息
     */
    public SystemException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }

    /**
     * 构造函数
     *
     * @param message 错误信息
     * @param cause   原始异常
     */
    public SystemException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public SystemException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @param cause     原始异常
     */
    public SystemException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }
}
