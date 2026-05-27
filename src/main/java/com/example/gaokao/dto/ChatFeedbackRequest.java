package com.example.gaokao.dto;

import lombok.Data;

@Data
public class ChatFeedbackRequest {

    private Integer feedbackStatus;
    private String feedbackComment;
}
