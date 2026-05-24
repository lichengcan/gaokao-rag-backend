package com.example.gaokao.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DifyStreamResult {

    private String conversationId;
    private String messageId;
    private List<ChatReference> references;
}
