package com.github.devlucasjava.socialklyp.application.mapper;

import com.github.devlucasjava.socialklyp.application.dto.response.comment.CommentResponse;
import com.github.devlucasjava.socialklyp.domain.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommentMapper {

    private final ProfileMapper profileMapper;

    public CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                profileMapper.toSummary(comment.getProfile()),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
