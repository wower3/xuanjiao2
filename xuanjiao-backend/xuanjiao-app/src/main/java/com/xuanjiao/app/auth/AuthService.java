package com.xuanjiao.app.auth;

import com.xuanjiao.client.dto.LoginCmd;
import com.xuanjiao.client.dto.LoginResultDTO;

public interface AuthService {
    LoginResultDTO login(LoginCmd cmd);
    void logout(String token);
}
