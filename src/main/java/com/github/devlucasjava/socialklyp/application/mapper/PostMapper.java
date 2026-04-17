package com.github.devlucasjava.socialklyp.application.mapper;

import com.github.devlucasjava.socialklyp.application.dto.request.post.CreatePostRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.media.MediaResponse;
import com.github.devlucasjava.socialklyp.application.dto.response.post.PostResponse;
import com.github.devlucasjava.socialklyp.domain.entity.Media;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
@RequiredArgsConstructor
public class PostMapper {

    private final MediaMapper mediaMapper;

    public PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getContent(),
                getFirstMedia(post),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public Post toEntity(CreatePostRequest request) {
        Post post = new Post();
        post.setContent(request.content());
        return post;
    }

    private MediaResponse getFirstMedia(Post post) {
        if (post.getMedias() == null || post.getMedias().isEmpty()) {
            return null;
        }

        Media firstMedia = post.getMedias().stream()
                .sorted(Comparator.comparing(Media::getCreatedAt))
                .findFirst()
                .orElse(null);

        return firstMedia != null ? mediaMapper.toResponse(firstMedia) : null;
    }
}