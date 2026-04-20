package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.response.like.LikeResponse;
import com.github.devlucasjava.socialklyp.application.mapper.LikeMapper;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ConflictException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Like;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.LikeRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.PostRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
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
    public LikeResponse likePost(UUID postId, User auth) {

        Profile profile = findProfileByUserAuthenticatedOrThrow(auth);
        Post post = findPostOrThrow(postId);

        boolean alreadyLiked = likeRepository.existsByProfileIdAndPostId(profile.getId(), postId);
        if (alreadyLiked) {
            throw new ConflictException("Profile already liked this post");
        }

        Like like = new Like();
        like.setPost(post);
        like.setProfile(profile);

        return likeMapper.toResponse(likeRepository.save(like));
    }

    @Transactional
    public void unlikePost(UUID postId, User auth) {

        Profile profile = findProfileByUserAuthenticatedOrThrow(auth);
        findPostOrThrow(postId);

        Like like = likeRepository.findByProfileIdAndPostId(profile.getId(), postId)
                .orElseThrow(() -> new ResourceNotFoundException("Like not found for this profile and post"));

        likeRepository.delete(like);
    }

    @Transactional(readOnly = true)
    public long countLikesByPost(UUID postId) {
        findPostOrThrow(postId);
        return likeRepository.countByPostId(postId);
    }

    private Post findPostOrThrow(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
    }

    private Profile findProfileByUserAuthenticatedOrThrow(User auth) {
        return profileRepository.findByUser(auth)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with user id: " + auth.getId()));
    }
}
