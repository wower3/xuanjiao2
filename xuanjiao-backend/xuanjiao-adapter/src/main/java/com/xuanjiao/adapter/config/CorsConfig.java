package com.xuanjiao.adapter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域资源共享(CORS)配置类
 *
 * <p>配置跨域请求策略，允许前端应用从不同域名访问后端 API。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>允许所有来源：使用 allowedOriginPatterns("*") 支持所有域名</li>
 *   <li>允许所有方法：GET、POST、PUT、DELETE、OPTIONS</li>
 *   <li>允许所有请求头：支持自定义请求头</li>
 *   <li>允许携带凭证：支持 Cookie 和认证信息</li>
 *   <li>预检请求缓存：缓存时间为 3600 秒</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 配置跨域映射
     *
     * <p>为所有路径添加 CORS 配置，允许跨域请求访问后端 API。
     * 此配置解决了前后端分离架构下的跨域问题。</p>
     *
     * @param registry CORS 注册器，用于添加跨域映射配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
