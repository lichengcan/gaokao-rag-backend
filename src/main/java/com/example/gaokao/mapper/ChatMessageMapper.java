package com.example.gaokao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.gaokao.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
