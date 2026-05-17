package com.github.devlucasjava.socialklyp.application.mapper;

import com.github.devlucasjava.socialklyp.application.dto.response.message.MessageResponse;
import com.github.devlucasjava.socialklyp.domain.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final ProfileMapper profileMapper;

    public MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .content(message.isDeleted() ? null : message.getContent())
                .sender(profileMapper.toSummary(message.getSender()))
                .deleted(message.isDeleted())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}
