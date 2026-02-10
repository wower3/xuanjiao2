package com.xuanjiao.adapter.interceptor;

import com.xuanjiao.app.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证拦截器
 *
 * <p>拦截所有需要认证的请求，验证 JWT 令牌的有效性。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>令牌验证：检查请求头中的 Authorization 令牌是否有效</li>
 *   <li>用户信息提取：从令牌中解析用户 ID 和用户名，存入请求属性</li>
 *   <li>预检请求放行：OPTIONS 请求直接放行，支持 CORS 预检</li>
 *   <li>错误响应：未登录或令牌无效时返回 401 状态码</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * JWT 工具类
     *
     * <p>用于解析和验证 JWT 令牌。</p>
     */
    @Resource
    private JwtUtil jwtUtil;

    /**
     * 请求预处理
     *
     * <p>在请求到达控制器之前进行身份验证：</p>
     * <ol>
     *   <li>检查是否为 OPTIONS 预检请求，如果是则直接放行</li>
     *   <li>从请求头获取 Authorization 令牌</li>
     *   <li>验证令牌是否存在，不存在则返回 401 未登录错误</li>
     *   <li>移除 Bearer 前缀并解析令牌</li>
     *   <li>提取用户 ID 和用户名，存入请求属性供后续使用</li>
     *   <li>令牌无效时返回 401 错误</li>
     * </ol>
     *
     * @param request HTTP 请求对象
     * @param response HTTP 响应对象
     * @param handler 处理器对象
     * @return true 表示继续执行后续处理，false 表示拦截请求
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }
        // 移除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"token无效\"}");
            return false;
        }
    }
}
