package com.github.devlucasjava.socialklyp.application.mapper;

import com.github.devlucasjava.socialklyp.application.dto.response.media.MediaResponse;
import com.github.devlucasjava.socialklyp.domain.entity.Media;
import com.github.devlucasjava.socialklyp.infrastructure.client.port.StoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MediaMapper {

    private final StoragePort storagePort;

    public MediaResponse toResponse(Media media) {
        MediaResponse dto = new MediaResponse();

        dto.setId(media.getId());
        dto.setMediaUrl(media.getMediaUrl());
        dto.setMediaName(media.getMediaName());
        dto.setMediaType(media.getMediaType().name());
        dto.setPostId(media.getPost().getId());
        dto.setCreatedAt(media.getCreatedAt());
        dto.setUpdatedAt(media.getUpdatedAt());

        return dto;
    }
}