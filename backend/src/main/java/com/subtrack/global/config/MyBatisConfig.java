package com.subtrack.global.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.subtrack.domain")
public class MyBatisConfig {
}
