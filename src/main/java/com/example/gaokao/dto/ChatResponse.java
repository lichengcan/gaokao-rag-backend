package com.example.gaokao.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {

    private String answer;
    private String conversationId;
    private Long messageId;
}
