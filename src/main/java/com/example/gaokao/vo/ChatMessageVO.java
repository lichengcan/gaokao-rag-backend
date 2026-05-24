package com.example.gaokao.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import com.example.gaokao.dto.ChatReference;

@Data
public class ChatMessageVO {

    private Long id;
    private String userId;
    private String conversationId;
    private String question;
    private String answer;
    private List<ChatReference> references;
    private LocalDateTime createTime;
}
