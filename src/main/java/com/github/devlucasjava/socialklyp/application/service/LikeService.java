package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.response.like.LikeResponse;
import com.github.devlucasjava.socialklyp.application.mapper.LikeMapper;
import com.github.devlucasjava.socialklyp.domain.entity.Like;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.LikeRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.PostRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final LikeMapper likeMapper;

    @Transactional
    public LikeResponse likePost(UUID postId, UUID profileId) {
        Post post = findPostOrThrow(postId);
        Profile profile = findProfileOrThrow(profileId);

        boolean alreadyLiked = likeRepository.existsByProfileIdAndPostId(profileId, postId);
        if (alreadyLiked) {
            throw new IllegalStateException("Profile has already liked this post");
        }

        Like like = new Like();
        like.setPost(post);
        like.setProfile(profile);

        return likeMapper.toResponse(likeRepository.save(like));
    }

    @Transactional
    public void unlikePost(UUID postId, UUID profileId) {
        findPostOrThrow(postId);
        findProfileOrThrow(profileId);

        Like like = likeRepository.findByProfileIdAndPostId(profileId, postId)
                .orElseThrow(() -> new EntityNotFoundException("Like not found for this profile and post"));

        likeRepository.delete(like);
    }

    @Transactional(readOnly = true)
    public long countLikesByPost(UUID postId) {
        findPostOrThrow(postId);
        return likeRepository.countByPostId(postId);
    }

    private Post findPostOrThrow(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
    }

    private Profile findProfileOrThrow(UUID profileId) {
        return profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + profileId));
    }
}
