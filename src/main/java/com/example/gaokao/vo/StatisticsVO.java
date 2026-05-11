package com.example.gaokao.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StatisticsVO {

    private Long totalQuestions;
    private Long todayQuestions;
    private Long conversationCount;
    private List<HotQuestion> hotQuestions;
    private List<ChatMessageVO> recentMessages;

    @Data
    @Builder
    public static class HotQuestion {
        private String question;
        private Long count;
    }
}
