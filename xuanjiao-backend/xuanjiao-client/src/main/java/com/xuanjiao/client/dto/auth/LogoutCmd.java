package com.xuanjiao.client.dto.auth;

import lombok.Data;

/**
 * 用户登出命令对象
 *
 * <p>封装用户登出所需的令牌信息，用于使当前会话失效。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class LogoutCmd {

    /**
     * JWT认证令牌
     */
    private String token;
}
