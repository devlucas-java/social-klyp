package com.github.devlucasjava.socialklyp.application.mapper;

import com.github.devlucasjava.socialklyp.application.dto.request.chat.CreateChatRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.chat.ChatResponse;
import com.github.devlucasjava.socialklyp.domain.entity.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMapper {

    private final ProfileMapper profileMapper;

    public ChatResponse toResponse(Chat chat) {
        return ChatResponse.builder()
                .id(chat.getId())
                .name(chat.getName())
                .description(chat.getDescription())
                .updatedAt(chat.getUpdatedAt())
                .createdAt(chat.getCreatedAt())
                .profileCreator(profileMapper.toSummary(chat.getProfileCreator()))
                .profiles(chat.getProfiles().stream()
                        .map(profileMapper::toSummary)
                        .toList())
                .profilesAdmin(chat.getProfilesAdmin().stream()
                        .map(profileMapper::toSummary)
                        .toList())
                .build();
    }

    public Chat toEntity(CreateChatRequest dto) {
        Chat chat = new Chat();
        chat.setName(dto.getName());
        chat.setDescription(dto.getDescription());
        return chat;
    }
}
