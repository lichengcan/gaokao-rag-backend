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
    private Long likedCount;
    private Long dislikedCount;
    private Long noReferenceCount;
    private Long unresolvedFeedbackCount;
    private Double feedbackRate;
    private List<HotQuestion> hotQuestions;
    private List<FeedbackItem> feedbackItems;
    private List<ChatMessageVO> recentMessages;

    @Data
    @Builder
    public static class HotQuestion {
        private String question;
        private Long count;
    }

    @Data
    @Builder
    public static class FeedbackItem {
        private Long id;
        private String userId;
        private String question;
        private String feedbackComment;
        private String createTime;
    }
}
