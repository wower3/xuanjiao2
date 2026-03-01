package com.xuanjiao.common.exception;

/**
 * 参数验证异常
 * <p>
 * 用于参数验证失败的场景，如：
 * - 角色类型不能为空
 * - 请至少上传一个素材文件
 * - 角色类型只能包含大写字母、数字和下划线
 *
 * @author xuanjiao
 * @date 2025-02-28
 */
public class ValidationException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "VALIDATION_ERROR";

    /**
     * 构造函数
     *
     * @param message 错误信息
     */
    public ValidationException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }

    /**
     * 构造函数
     *
     * @param message 错误信息
     * @param cause   原始异常
     */
    public ValidationException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @param cause     原始异常
     */
    public ValidationException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }
}
