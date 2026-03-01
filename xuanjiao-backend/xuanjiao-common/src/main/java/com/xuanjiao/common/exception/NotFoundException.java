package com.xuanjiao.common.exception;

/**
 * 资源未找到异常
 * <p>
 * 用于资源不存在的场景，如：
 * - 用户不存在
 * - 流程不存在
 * - 任务不存在
 * - 素材不存在
 *
 * @author xuanjiao
 * @date 2025-02-28
 */
public class NotFoundException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "NOT_FOUND";

    /**
     * 构造函数
     *
     * @param message 错误信息
     */
    public NotFoundException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }

    /**
     * 构造函数
     *
     * @param message 错误信息
     * @param cause   原始异常
     */
    public NotFoundException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public NotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @param cause     原始异常
     */
    public NotFoundException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * 便捷构造函数：根据资源类型和ID生成错误信息
     *
     * @param resourceType 资源类型（如"用户"、"流程"）
     * @param id           资源ID
     */
    public NotFoundException(String resourceType, Object id) {
        super(DEFAULT_ERROR_CODE, resourceType + "不存在: " + id);
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }
}
