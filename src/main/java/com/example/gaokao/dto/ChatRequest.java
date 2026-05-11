package com.example.gaokao.dto;

import lombok.Data;

@Data
public class ChatRequest {

    private String userId;
    private String question;
    private String conversationId;
}
