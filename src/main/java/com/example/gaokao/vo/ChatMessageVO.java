package com.example.gaokao.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {

    private Long id;
    private String userId;
    private String conversationId;
    private String question;
    private String answer;
    private LocalDateTime createTime;
}
