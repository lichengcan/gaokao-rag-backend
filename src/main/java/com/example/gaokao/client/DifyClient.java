package com.example.gaokao.client;

import com.example.gaokao.common.exception.DifyApiException;
import com.example.gaokao.config.DifyProperties;
import com.example.gaokao.dto.DifyChatResponse;
import com.example.gaokao.dto.DifyStreamResult;
import com.example.gaokao.dto.ChatReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
@RequiredArgsConstructor
public class DifyClient {

    private final DifyProperties difyProperties;
    private final ObjectMapper objectMapper;

    public void checkHealth() {
        ensureConfigured();
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(difyProperties.getConnectTimeoutMs()))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(Math.min(difyProperties.getReadTimeoutMs(), 10000)));

        RestClient restClient = RestClient.builder()
                .baseUrl(normalizeBaseUrl(difyProperties.getBaseUrl()))
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + difyProperties.getApiKey())
                .build();

        try {
            restClient.get().uri("/parameters").retrieve().body(String.class);
        } catch (Exception e) {
            if (e instanceof RestClientResponseException restException) {
                throw new DifyApiException(classifyDifyError(restException.getResponseBodyAsString()),
                        extractDifyError(restException.getResponseBodyAsString()), e);
            }
            throw new DifyApiException("DIFY_UNAVAILABLE", "Dify 健康检查失败，请检查网络或服务配置。", e);
        }
    }

    public DifyChatResponse chat(String userId, String question, String conversationId) {
        ensureConfigured();

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
        body.put("response_mode", "blocking");
        body.put("conversation_id", StringUtils.hasText(conversationId) ? conversationId : "");
        body.put("user", userId);

        try {
            String response = restClient.post()
                    .uri("/chat-messages")
                    .body(body)
                    .retrieve()
                    .body(String.class);
            DifyChatResponse chatResponse = objectMapper.readValue(response, DifyChatResponse.class);
            chatResponse.setReferences(extractReferences(objectMapper.readTree(response)));
            if (chatResponse == null || !StringUtils.hasText(chatResponse.getAnswer())) {
                throw new DifyApiException("Dify response is empty");
            }
            return chatResponse;
        } catch (Exception e) {
            if (e instanceof DifyApiException) {
                throw (DifyApiException) e;
            }
            if (e instanceof RestClientResponseException restException) {
                throw new DifyApiException(classifyDifyError(restException.getResponseBodyAsString()),
                        extractDifyError(restException.getResponseBodyAsString()), e);
            }
            throw new DifyApiException("DIFY_UNAVAILABLE", "Dify API 调用失败，请检查网络或服务配置。", e);
        }
    }

    public DifyStreamResult streamChat(String userId, String question, String conversationId, Consumer<String> answerConsumer) {
        ensureConfigured();

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
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .build();

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", Map.of());
        body.put("query", question);
        body.put("response_mode", "streaming");
        body.put("conversation_id", StringUtils.hasText(conversationId) ? conversationId : "");
        body.put("user", userId);

        try {
            return restClient.post()
                    .uri("/chat-messages")
                    .body(body)
                    .exchange((request, response) -> {
                        if (response.getStatusCode().isError()) {
                            String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                            throw new DifyApiException(classifyDifyError(responseBody), extractDifyError(responseBody));
                        }

                        StreamState state = new StreamState();
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                            StringBuilder eventData = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.isBlank()) {
                                    handleStreamEvent(eventData, state, answerConsumer);
                                    eventData.setLength(0);
                                    continue;
                                }
                                if (line.startsWith("data:")) {
                                    eventData.append(line.substring(5).trim());
                                }
                            }
                            handleStreamEvent(eventData, state, answerConsumer);
                        }

                        return DifyStreamResult.builder()
                                .conversationId(state.conversationId)
                                .messageId(state.messageId)
                                .references(state.references)
                                .build();
                    });
        } catch (Exception e) {
            if (e instanceof DifyApiException) {
                throw (DifyApiException) e;
            }
            if (e instanceof RestClientResponseException restException) {
                throw new DifyApiException(classifyDifyError(restException.getResponseBodyAsString()),
                        extractDifyError(restException.getResponseBodyAsString()), e);
            }
            throw new DifyApiException("DIFY_UNAVAILABLE", "Dify 流式调用失败，请检查网络或服务配置。", e);
        }
    }

    private void ensureConfigured() {
        if (!StringUtils.hasText(difyProperties.getApiKey()) || "your-dify-api-key".equals(difyProperties.getApiKey())) {
            throw new DifyApiException("DIFY_CONFIG_ERROR", "Dify API key 未配置。");
        }
    }

    private void handleStreamEvent(StringBuilder eventData, StreamState state, Consumer<String> answerConsumer) {
        if (eventData.length() == 0) {
            return;
        }

        String data = eventData.toString();
        if ("[DONE]".equals(data)) {
            return;
        }

        try {
            JsonNode node = objectMapper.readTree(data);
            String event = textValue(node, "event");
            if ("error".equals(event)) {
                String message = textValue(node, "message");
                throw new DifyApiException(classifyDifyError(message), StringUtils.hasText(message)
                        ? "Dify API 调用失败：" + message
                        : "Dify streaming API returned error event");
            }

            String streamError = streamError(node);
            if (StringUtils.hasText(streamError)) {
                throw new DifyApiException(classifyDifyError(streamError), "Dify API 调用失败：" + streamError);
            }

            updateState(node, state);
            addReferences(state.references, extractReferences(node));

            String answer = textValue(node, "answer");
            if (StringUtils.hasText(answer) && ("message".equals(event) || "agent_message".equals(event))) {
                answerConsumer.accept(answer);
            }
            String workflowAnswer = workflowAnswer(node);
            if (StringUtils.hasText(workflowAnswer) && "workflow_finished".equals(event)) {
                answerConsumer.accept(workflowAnswer);
            }
        } catch (DifyApiException e) {
            throw e;
        } catch (Exception e) {
            throw new DifyApiException("Failed to parse Dify streaming event", e);
        }
    }

    private void updateState(JsonNode node, StreamState state) {
        String conversationId = textValue(node, "conversation_id");
        if (StringUtils.hasText(conversationId)) {
            state.conversationId = conversationId;
        }

        String messageId = textValue(node, "message_id");
        if (StringUtils.hasText(messageId)) {
            state.messageId = messageId;
        }
    }

    private String streamError(JsonNode node) {
        JsonNode data = node.get("data");
        if (data == null || data.isNull()) {
            return null;
        }
        String status = textValue(data, "status");
        String error = textValue(data, "error");
        if ("failed".equals(status) && StringUtils.hasText(error)) {
            return error;
        }
        return null;
    }

    private String workflowAnswer(JsonNode node) {
        JsonNode data = node.get("data");
        if (data == null || data.isNull()) {
            return null;
        }
        JsonNode outputs = data.get("outputs");
        if (outputs == null || outputs.isNull()) {
            return null;
        }

        for (String fieldName : new String[]{"answer", "text", "output", "result"}) {
            String value = textValue(outputs, fieldName);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private List<ChatReference> extractReferences(JsonNode root) {
        List<ChatReference> references = new ArrayList<>();
        collectReferences(root, references);
        return references;
    }

    private void collectReferences(JsonNode node, List<ChatReference> references) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isArray()) {
            node.forEach(item -> collectReferences(item, references));
            return;
        }
        if (!node.isObject()) {
            return;
        }

        if (looksLikeReference(node)) {
            addReference(references, node);
        }

        node.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode value = entry.getValue();
            if ("retriever_resources".equals(fieldName)
                    || "retriever_resource".equals(fieldName)
                    || "result".equals(fieldName)
                    || "results".equals(fieldName)
                    || "documents".equals(fieldName)
                    || "chunks".equals(fieldName)
                    || "records".equals(fieldName)
                    || "outputs".equals(fieldName)
                    || "metadata".equals(fieldName)
                    || "data".equals(fieldName)) {
                collectReferences(value, references);
            }
        });
    }

    private boolean looksLikeReference(JsonNode node) {
        return StringUtils.hasText(firstText(node, "content", "text", "segment_content"))
                && StringUtils.hasText(firstText(node, "document_name", "title", "source", "dataset_name", "doc_name"));
    }

    private void addReference(List<ChatReference> references, JsonNode node) {
        String content = firstText(node, "content", "text", "segment_content");
        String title = firstText(node, "document_name", "title", "doc_name", "dataset_name", "source");
        String source = firstText(node, "source", "dataset_name", "document_name", "doc_name");
        Double score = firstDouble(node, "score", "similarity", "vector_score");

        boolean exists = references.stream().anyMatch(reference ->
                sameText(reference.getTitle(), title) && sameText(reference.getContent(), content));
        if (!exists) {
            references.add(ChatReference.builder()
                    .title(title)
                    .source(source)
                    .content(content)
                    .score(score)
                    .build());
        }
    }

    private void addReferences(List<ChatReference> target, List<ChatReference> source) {
        source.forEach(reference -> {
            boolean exists = target.stream().anyMatch(existing ->
                    sameText(existing.getTitle(), reference.getTitle())
                            && sameText(existing.getContent(), reference.getContent()));
            if (!exists) {
                target.add(reference);
            }
        });
    }

    private boolean sameText(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private String firstText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String value = textValue(node, fieldName);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private Double firstDouble(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = node.get(fieldName);
            if (value == null || value.isNull()) {
                continue;
            }
            if (value.isNumber()) {
                return value.asDouble();
            }
            if (value.isTextual()) {
                try {
                    return Double.parseDouble(value.asText());
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "https://api.dify.ai/v1";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String extractDifyError(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "Dify API 调用失败，请检查 Dify 应用配置。";
        }
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            String message = textValue(node, "message");
            if (StringUtils.hasText(message)) {
                return "Dify API 调用失败：" + message;
            }
        } catch (Exception ignored) {
            // Use the generic message below when Dify returns a non-JSON error body.
        }
        return "Dify API 调用失败，请检查 Dify 应用配置。";
    }

    private String classifyDifyError(String text) {
        String message = text == null ? "" : text.toLowerCase();
        if (message.contains("api key") || message.contains("unauthorized") || message.contains("forbidden")) {
            return "DIFY_AUTH_ERROR";
        }
        if (message.contains("model is not configured") || message.contains("metadata_model_config")
                || message.contains("app unavailable") || message.contains("app configurations")
                || message.contains("not_workflow_app")) {
            return "DIFY_CONFIG_ERROR";
        }
        if (message.contains("timeout") || message.contains("timed out")) {
            return "DIFY_TIMEOUT";
        }
        if (message.contains("rate limit") || message.contains("too many requests")) {
            return "DIFY_RATE_LIMIT";
        }
        return "DIFY_ERROR";
    }

    private static class StreamState {
        private String conversationId;
        private String messageId;
        private final List<ChatReference> references = new ArrayList<>();
    }
}
