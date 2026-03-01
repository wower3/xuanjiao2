package com.xuanjiao.common.exception;

/**
 * 权限异常
 * <p>
 * 用于权限不足的场景，如：
 * - 无权操作
 * - 无权分配角色
 * - 只有管理员才能执行此操作
 *
 * @author xuanjiao
 * @date 2025-02-28
 */
public class PermissionException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "PERMISSION_DENIED";

    /**
     * 构造函数
     *
     * @param message 错误信息
     */
    public PermissionException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }

    /**
     * 构造函数
     *
     * @param message 错误信息
     * @param cause   原始异常
     */
    public PermissionException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public PermissionException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @param cause     原始异常
     */
    public PermissionException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }
}
