package com.github.devlucasjava.socialklyp.application.mapper;

import com.github.devlucasjava.socialklyp.application.dto.response.like.LikeResponse;
import com.github.devlucasjava.socialklyp.domain.entity.Like;
import org.springframework.stereotype.Component;

@Component
public class LikeMapper {

    public LikeResponse toResponse(Like like) {
        return new LikeResponse(
                like.getId(),
                like.getProfile().getId(),
                like.getPost().getId()
        );
    }
}
