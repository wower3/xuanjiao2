package com.xuanjiao.client.dto.auth;

import lombok.Data;

/**
 * 用户登出命令对象
 */
@Data
public class LogoutCmd {

    private String token;
}
