package com.example.gaokao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gaokao.client.DifyClient;
import com.example.gaokao.common.exception.BusinessException;
import com.example.gaokao.dto.ChatRequest;
import com.example.gaokao.dto.ChatResponse;
import com.example.gaokao.dto.DifyChatResponse;
import com.example.gaokao.entity.ChatMessage;
import com.example.gaokao.mapper.ChatMessageMapper;
import com.example.gaokao.service.ChatService;
import com.example.gaokao.vo.ChatMessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatService {

    private final DifyClient difyClient;

    public ChatServiceImpl(DifyClient difyClient) {
        this.difyClient = difyClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatResponse send(ChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getQuestion())) {
            throw new BusinessException("请输入需要咨询的问题。");
        }
        String userId = StringUtils.hasText(request.getUserId()) ? request.getUserId() : "test-user-001";
        String conversationId = StringUtils.hasText(request.getConversationId()) ? request.getConversationId().trim() : "";
        if (conversationId.length() > 100) {
            throw new BusinessException("当前会话异常，请重新发起对话。");
        }

        DifyChatResponse difyResponse = difyClient.chat(userId, request.getQuestion().trim(), conversationId);

        ChatMessage message = new ChatMessage();
        message.setUserId(userId);
        message.setConversationId(difyResponse.getConversationId());
        message.setQuestion(request.getQuestion().trim());
        message.setAnswer(difyResponse.getAnswer());
        message.setMessageSource("DIFY");
        message.setStatus(1);
        save(message);

        return ChatResponse.builder()
                .answer(message.getAnswer())
                .conversationId(message.getConversationId())
                .messageId(message.getId())
                .build();
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
        return vo;
    }
}
