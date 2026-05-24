package com.example.gaokao.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatReference {

    private String title;
    private String source;
    private String content;
    private Double score;
}
