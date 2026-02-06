package com.xuanjiao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

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

    public static void main(String[] args) {
        SpringApplication.run(XuanjiaoApplication.class, args);
    }
}
