package com.example.gaokao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gaokao.dto.ChatRequest;
import com.example.gaokao.dto.ChatResponse;
import com.example.gaokao.entity.ChatMessage;
import com.example.gaokao.vo.ChatMessageVO;

import java.util.List;

public interface ChatService extends IService<ChatMessage> {

    ChatResponse send(ChatRequest request);

    List<ChatMessageVO> history(String userId, String keyword);

    List<ChatMessageVO> conversation(String conversationId);

    void deleteMessage(Long id);
}
