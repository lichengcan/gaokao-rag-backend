package com.example.gaokao.client;

import com.example.gaokao.common.exception.DifyApiException;
import com.example.gaokao.config.DifyProperties;
import com.example.gaokao.dto.DifyChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DifyClient {

    private final DifyProperties difyProperties;
    private final ObjectMapper objectMapper;

    public DifyChatResponse chat(String userId, String question, String conversationId) {
        if (!StringUtils.hasText(difyProperties.getApiKey()) || "your-dify-api-key".equals(difyProperties.getApiKey())) {
            throw new DifyApiException("Dify API key is not configured");
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(difyProperties.getConnectTimeoutMs()))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(difyProperties.getReadTimeoutMs()));

        RestClient restClient = RestClient.builder()
                .baseUrl(normalizeBaseUrl(difyProperties.getBaseUrl()))
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + difyProperties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", Map.of());
        body.put("query", question);
        body.put("response_mode", difyProperties.getResponseMode());
        body.put("conversation_id", StringUtils.hasText(conversationId) ? conversationId : "");
        body.put("user", userId);

        try {
            String response = restClient.post()
                    .uri("/chat-messages")
                    .body(body)
                    .retrieve()
                    .body(String.class);
            DifyChatResponse chatResponse = objectMapper.readValue(response, DifyChatResponse.class);
            if (chatResponse == null || !StringUtils.hasText(chatResponse.getAnswer())) {
                throw new DifyApiException("Dify response is empty");
            }
            return chatResponse;
        } catch (Exception e) {
            if (e instanceof DifyApiException difyApiException) {
                throw difyApiException;
            }
            throw new DifyApiException("Failed to call Dify API", e);
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "https://api.dify.ai/v1";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
