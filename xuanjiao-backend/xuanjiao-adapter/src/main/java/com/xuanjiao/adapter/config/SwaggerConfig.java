package com.xuanjiao.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger API 文档配置类
 *
 * <p>配置 Swagger UI 用于生成和展示 RESTful API 文档。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>API 文档生成：自动扫描控制器类生成 API 文档</li>
 *   <li>Swagger UI：提供可视化的 API 测试界面</li>
 *   <li>访问地址：http://localhost:8080/api/doc.html</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Configuration
public class SwaggerConfig {

    /**
     * 创建 RESTful API 的 Docket 实例
     *
     * <p>配置 Swagger 扫描 com.xuanjiao.adapter.web 包下的所有控制器，
     * 生成对应的 API 文档。</p>
     *
     * @return Docket 实例，包含 API 文档的配置信息
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.xuanjiao.adapter.web"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 构建 API 文档的基本信息
     *
     * <p>设置文档标题、描述和版本号。</p>
     *
     * @return ApiInfo 实例，包含 API 文档的基本信息
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("宣传教育平台 API")
                .description("宣传教育平台接口文档")
                .version("1.0.0")
                .build();
    }
}
