package com.xuanjiao.client;

import lombok.Data;

/**
 * 统一API响应结果数据传输对象
 *
 * <p>封装所有API接口的统一响应格式，包含状态码、消息和数据。
 * 支持泛型，可包装任意类型的响应数据。</p>
 *
 * @param <T> 响应数据的类型
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class Result<T> {

    /**
     * 响应状态码（200表示成功，其他表示错误）
     */
    private Integer code;

    /**
     * 响应消息（成功时为"success"，失败时为错误描述）
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 创建成功响应（无数据）
     *
     * @param <T> 响应数据的类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 创建成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  响应数据的类型
     * @return 成功响应对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 创建错误响应（默认500状态码）
     *
     * @param message 错误消息
     * @param <T>     响应数据的类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /**
     * 创建错误响应（指定状态码）
     *
     * @param code    错误状态码
     * @param message 错误消息
     * @param <T>     响应数据的类型
     * @return 错误响应对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
