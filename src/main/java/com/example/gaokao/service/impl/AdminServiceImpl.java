package com.example.gaokao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.gaokao.entity.ChatMessage;
import com.example.gaokao.mapper.ChatMessageMapper;
import com.example.gaokao.vo.ChatMessageVO;
import com.example.gaokao.vo.StatisticsVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements com.example.gaokao.service.AdminService {

    private final ChatMessageMapper chatMessageMapper;

    public AdminServiceImpl(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    @Override
    public StatisticsVO statistics() {
        LambdaQueryWrapper<ChatMessage> normal = new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getStatus, 1);
        Long total = chatMessageMapper.selectCount(normal);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long today = chatMessageMapper.selectCount(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getStatus, 1)
                .ge(ChatMessage::getCreateTime, todayStart));

        List<ChatMessage> allMessages = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getStatus, 1));
        long conversationCount = allMessages.stream()
                .map(ChatMessage::getConversationId)
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .distinct()
                .count();

        List<StatisticsVO.HotQuestion> hotQuestions = allMessages.stream()
                .collect(Collectors.groupingBy(ChatMessage::getQuestion, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> StatisticsVO.HotQuestion.builder().question(entry.getKey()).count(entry.getValue()).build())
                .toList();

        List<ChatMessageVO> recentMessages = chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getStatus, 1)
                        .orderByDesc(ChatMessage::getCreateTime)
                        .last("LIMIT 10"))
                .stream().map(this::toVO).toList();

        return StatisticsVO.builder()
                .totalQuestions(total)
                .todayQuestions(today)
                .conversationCount(conversationCount)
                .hotQuestions(hotQuestions)
                .recentMessages(recentMessages)
                .build();
    }

    private ChatMessageVO toVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(message, vo);
        return vo;
    }
}
