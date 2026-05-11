package com.example.gaokao.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dify")
public class DifyProperties {

    private String baseUrl = "https://api.dify.ai/v1";
    private String apiKey;
    private String responseMode = "blocking";
    private Integer connectTimeoutMs = 10000;
    private Integer readTimeoutMs = 60000;
}
