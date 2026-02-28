package com.xuanjiao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 宣传教育平台启动类
 *
 * <p>Spring Boot 应用程序入口点，负责初始化应用程序上下文。
 * 该类是整个宣传教育平台的核心启动类，采用 COLA 架构分层设计。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>启用 Spring Boot 自动配置</li>
 *   <li>启用定时任务调度</li>
 *   <li>扫描并注册 MyBatis Mapper 接口</li>
 * </ul>
 *
 * <p>启动方式：</p>
 * <pre>
 * # 方式一：Maven 启动
 * mvn spring-boot:run -pl xuanjiao-start
 *
 * # 方式二：JAR 包启动
 * java -jar xuanjiao-start.jar
 *
 * # 方式三：指定配置文件启动
 * java -jar xuanjiao-start.jar --spring.profiles.active=dev
 * </pre>
 *
 * <p>扫描的 Mapper 包路径：</p>
 * <ul>
 *   <li>com.xuanjiao.infrastructure.user - 用户模块</li>
 *   <li>com.xuanjiao.infrastructure.dept - 部门模块</li>
 *   <li>com.xuanjiao.infrastructure.role - 角色模块</li>
 *   <li>com.xuanjiao.infrastructure.menu - 菜单模块</li>
 *   <li>com.xuanjiao.infrastructure.asset - 素材模块</li>
 *   <li>com.xuanjiao.infrastructure.material - 素材申请模块</li>
 *   <li>com.xuanjiao.infrastructure.usage - 使用申请模块</li>
 *   <li>com.xuanjiao.infrastructure.workflow - 工作流模块</li>
 *   <li>com.xuanjiao.infrastructure.approval - 审批模块</li>
 *   <li>com.xuanjiao.infrastructure.deletion - 删除模块</li>
 *   <li>com.xuanjiao.infrastructure.log - 日志模块</li>
 *   <li>com.xuanjiao.infrastructure.notification - 通知模块</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.scheduling.annotation.EnableScheduling
 * @see org.mybatis.spring.annotation.MapperScan
 */
@SpringBootApplication
@EnableScheduling
@MapperScan({
    "com.xuanjiao.infrastructure.user",
    "com.xuanjiao.infrastructure.dept",
    "com.xuanjiao.infrastructure.role",
    "com.xuanjiao.infrastructure.menu",
    "com.xuanjiao.infrastructure.asset",
    "com.xuanjiao.infrastructure.material",
    "com.xuanjiao.infrastructure.usage",
    "com.xuanjiao.infrastructure.workflow",
    "com.xuanjiao.infrastructure.approval",
    "com.xuanjiao.infrastructure.deletion",
    "com.xuanjiao.infrastructure.log",
    "com.xuanjiao.infrastructure.notification"
})
public class XuanjiaoApplication {

    /**
     * 应用程序入口点
     *
     * <p>启动 Spring Boot 应用程序，初始化 IoC 容器，
     * 加载所有配置、Bean 和自动配置类。</p>
     *
     * @param args 命令行参数，支持 Spring Boot 标准参数：
     *             <ul>
     *               <li>--server.port=8080 指定服务端口</li>
     *               <li>--spring.profiles.active=dev 指定环境配置</li>
     *               <li>--spring.datasource.url=xxx 覆盖数据源配置</li>
     *             </ul>
     */
    public static void main(String[] args) {
        SpringApplication.run(XuanjiaoApplication.class, args);
    }
}
