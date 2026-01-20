package com.xuanjiao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({
    "com.xuanjiao.infrastructure.user",
    "com.xuanjiao.infrastructure.dept",
    "com.xuanjiao.infrastructure.role",
    "com.xuanjiao.infrastructure.menu",
    "com.xuanjiao.infrastructure.asset",
    "com.xuanjiao.infrastructure.material",
    "com.xuanjiao.infrastructure.usage",
    "com.xuanjiao.infrastructure.workflow",
    "com.xuanjiao.infrastructure.approval"
})
public class XuanjiaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(XuanjiaoApplication.class, args);
    }
}
