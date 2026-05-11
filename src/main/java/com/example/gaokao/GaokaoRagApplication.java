package com.example.gaokao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.example.gaokao.mapper")
@ConfigurationPropertiesScan
public class GaokaoRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(GaokaoRagApplication.class, args);
    }
}
