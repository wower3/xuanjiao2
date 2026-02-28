package com.xuanjiao.adapter.config;

import com.xuanjiao.adapter.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.annotation.Resource;

/**
 * Web MVC 配置类
 *
 * <p>配置 Spring MVC 相关设置，包括拦截器注册等。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>拦截器配置：注册认证拦截器，对所有请求进行身份验证</li>
 *   <li>路径排除：排除登录、静态资源、API 文档等无需认证的路径</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 认证拦截器
     *
     * <p>用于验证请求中的 JWT 令牌，确保用户已登录。</p>
     */
    @Resource
    private AuthInterceptor authInterceptor;

    /**
     * 添加拦截器配置
     *
     * <p>注册认证拦截器，对除以下路径外的所有请求进行身份验证：</p>
     * <ul>
     *   <li>/auth/login - 用户登录接口</li>
     *   <li>/asset/preview/** - 素材预览接口</li>
     *   <li>/asset/thumbnail/** - 缩略图查看接口</li>
     *   <li>/doc.html - Swagger UI 页面</li>
     *   <li>/webjars/** - Swagger UI 静态资源</li>
     *   <li>/swagger-resources/** - Swagger 资源</li>
     *   <li>/v2/api-docs - Swagger API 文档</li>
     * </ul>
     *
     * @param registry 拦截器注册器，用于注册和配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/asset/preview/**", "/asset/thumbnail/**", "/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs");
    }
}
