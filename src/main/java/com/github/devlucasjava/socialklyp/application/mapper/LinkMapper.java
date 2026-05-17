package com.github.devlucasjava.socialklyp.application.mapper;

import com.github.devlucasjava.socialklyp.application.dto.response.link.LinkResponse;
import com.github.devlucasjava.socialklyp.domain.entity.Link;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkMapper {

    private final ProfileMapper profileMapper;

    public LinkResponse toResponse(Link link) {
        return LinkResponse.builder()
                .id(link.getId())
                .chatId(link.getChat().getId())
                .active(link.isActive())
                .usersSubscribed(link.getUsersSubscribed())
                .profileCreate(profileMapper.toSummary(link.getProfile()))
                .createdAt(link.getCreatedAt())
                .expirationDate(link.getExpirationDate())
                .build();
    }
}
