package com.xuanjiao.common.exception;

/**
 * 业务异常
 * <p>
 * 用于业务逻辑验证失败的场景，如：
 * - 状态验证：只有草稿状态可以修改
 * - 业务规则：只能使用已通过审批的素材
 * - 操作限制：只有发起人才能追回工单
 *
 * @author xuanjiao
 * @date 2025-02-28
 */
public class BusinessException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "BUSINESS_ERROR";

    /**
     * 构造函数
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(DEFAULT_ERROR_CODE, message);
    }

    /**
     * 构造函数
     *
     * @param message 错误信息
     * @param cause   原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(DEFAULT_ERROR_CODE, message, cause);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @param cause     原始异常
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }
}
