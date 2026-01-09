package com.xuanjiao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xuanjiao.infrastructure.mapper")
public class XuanjiaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(XuanjiaoApplication.class, args);
    }
}
