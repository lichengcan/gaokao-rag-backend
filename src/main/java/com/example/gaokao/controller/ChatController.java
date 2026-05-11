package com.example.gaokao.controller;

import com.example.gaokao.common.Result;
import com.example.gaokao.dto.ChatRequest;
import com.example.gaokao.dto.ChatResponse;
import com.example.gaokao.service.ChatService;
import com.example.gaokao.vo.ChatMessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public Result<ChatResponse> send(@RequestBody ChatRequest request) {
        return Result.success(chatService.send(request));
    }

    @GetMapping("/history")
    public Result<List<ChatMessageVO>> history(@RequestParam(required = false) String userId,
                                               @RequestParam(required = false) String keyword) {
        return Result.success(chatService.history(userId, keyword));
    }

    @GetMapping("/conversation/{conversationId}")
    public Result<List<ChatMessageVO>> conversation(@PathVariable String conversationId) {
        return Result.success(chatService.conversation(conversationId));
    }

    @DeleteMapping("/message/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        chatService.deleteMessage(id);
        return Result.success();
    }
}
