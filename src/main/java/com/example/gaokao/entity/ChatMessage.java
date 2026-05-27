package com.example.gaokao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String conversationId;
    private String question;
    private String answer;
    private String referencesJson;
    private Integer feedbackStatus;
    private String feedbackComment;
    private String messageSource;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
