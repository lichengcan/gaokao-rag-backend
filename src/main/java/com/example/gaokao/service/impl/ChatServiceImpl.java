package com.example.gaokao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gaokao.client.DifyClient;
import com.example.gaokao.common.exception.BusinessException;
import com.example.gaokao.dto.ChatRequest;
import com.example.gaokao.dto.ChatReference;
import com.example.gaokao.dto.ChatResponse;
import com.example.gaokao.dto.DifyChatResponse;
import com.example.gaokao.dto.DifyStreamResult;
import com.example.gaokao.entity.ChatMessage;
import com.example.gaokao.mapper.ChatMessageMapper;
import com.example.gaokao.service.ChatService;
import com.example.gaokao.vo.ChatMessageVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Service
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatService {

    private final DifyClient difyClient;
    private final TaskExecutor taskExecutor;
    private final ObjectMapper objectMapper;

    public ChatServiceImpl(DifyClient difyClient, TaskExecutor taskExecutor, ObjectMapper objectMapper) {
        this.difyClient = difyClient;
        this.taskExecutor = taskExecutor;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatResponse send(ChatRequest request) {
        ChatContext context = validateAndBuildContext(request);

        DifyChatResponse difyResponse = difyClient.chat(context.userId(), context.question(), context.conversationId());

        ChatMessage message = new ChatMessage();
        message.setUserId(context.userId());
        message.setConversationId(difyResponse.getConversationId());
        message.setQuestion(context.question());
        message.setAnswer(difyResponse.getAnswer());
        message.setReferencesJson(toReferencesJson(difyResponse.getReferences()));
        message.setMessageSource("DIFY");
        message.setStatus(1);
        save(message);

        return ChatResponse.builder()
                .answer(message.getAnswer())
                .conversationId(message.getConversationId())
                .messageId(message.getId())
                .references(difyResponse.getReferences())
                .build();
    }

    @Override
    public SseEmitter stream(ChatRequest request) {
        ChatContext context = validateAndBuildContext(request);
        SseEmitter emitter = new SseEmitter(0L);

        taskExecutor.execute(() -> {
            StringBuilder fullAnswer = new StringBuilder();
            try {
                DifyStreamResult streamResult = difyClient.streamChat(
                        context.userId(),
                        context.question(),
                        context.conversationId(),
                        answer -> {
                            fullAnswer.append(answer);
                            sendEvent(emitter, "message", Map.of("content", answer));
                        });

                if (fullAnswer.isEmpty()) {
                    throw new BusinessException("AI 服务暂时不可用，请稍后重试。");
                }

                ChatMessage message = new ChatMessage();
                message.setUserId(context.userId());
                message.setConversationId(StringUtils.hasText(streamResult.getConversationId())
                        ? streamResult.getConversationId()
                        : context.conversationId());
                message.setQuestion(context.question());
                message.setAnswer(fullAnswer.toString());
                message.setReferencesJson(toReferencesJson(streamResult.getReferences()));
                message.setMessageSource("DIFY");
                message.setStatus(1);
                save(message);

                sendEvent(emitter, "end", Map.of(
                        "conversationId", message.getConversationId(),
                        "messageId", message.getId(),
                        "references", streamResult.getReferences() == null ? List.of() : streamResult.getReferences()
                ));
                emitter.complete();
            } catch (Exception e) {
                String message = e.getMessage();
                if (!StringUtils.hasText(message)) {
                    message = "AI 服务暂时不可用，请稍后重试。";
                }
                try {
                    sendEvent(emitter, "error", Map.of("message", message));
                } finally {
                    emitter.complete();
                }
            }
        });

        return emitter;
    }

    @Override
    public List<ChatMessageVO> history(String userId, String keyword) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getStatus, 1)
                .orderByDesc(ChatMessage::getCreateTime);
        if (StringUtils.hasText(userId)) {
            wrapper.eq(ChatMessage::getUserId, userId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ChatMessage::getQuestion, keyword).or().like(ChatMessage::getAnswer, keyword));
        }
        return list(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public List<ChatMessageVO> conversation(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            throw new BusinessException("当前会话异常，请重新发起对话。");
        }
        return list(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId)
                .eq(ChatMessage::getStatus, 1)
                .orderByAsc(ChatMessage::getCreateTime))
                .stream().map(this::toVO).toList();
    }

    @Override
    public void deleteMessage(Long id) {
        ChatMessage message = getById(id);
        if (message != null) {
            message.setStatus(0);
            updateById(message);
        }
    }

    private ChatMessageVO toVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(message, vo);
        vo.setReferences(parseReferences(message.getReferencesJson()));
        return vo;
    }

    private String toReferencesJson(List<ChatReference> references) {
        if (references == null || references.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(references);
        } catch (Exception e) {
            return null;
        }
    }

    private List<ChatReference> parseReferences(String referencesJson) {
        if (!StringUtils.hasText(referencesJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(referencesJson, new TypeReference<List<ChatReference>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private ChatContext validateAndBuildContext(ChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getQuestion())) {
            throw new BusinessException("请输入需要咨询的问题。");
        }
        String userId = StringUtils.hasText(request.getUserId()) ? request.getUserId() : "test-user-001";
        String conversationId = StringUtils.hasText(request.getConversationId()) ? request.getConversationId().trim() : "";
        if (conversationId.length() > 100) {
            throw new BusinessException("当前会话异常，请重新发起对话。");
        }
        return new ChatContext(userId, request.getQuestion().trim(), conversationId);
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException e) {
            throw new BusinessException("消息发送失败，请重新发起对话。");
        }
    }

    private record ChatContext(String userId, String question, String conversationId) {
    }
}
