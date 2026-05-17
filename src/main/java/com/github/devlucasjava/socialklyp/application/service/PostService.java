package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.request.post.CreatePostRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.post.UpdatePostRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.post.PostResponse;
import com.github.devlucasjava.socialklyp.application.mapper.PostMapper;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ForbiddenException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.FollowRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.PostRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final FollowRepository followRepository;
    private final PostMapper postMapper;

    @Transactional(readOnly = true)
    public Page<PostResponse> findAll(Pageable pageable) {
        return postRepository.findByProfileIsPrivateFalse(pageable).map(postMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> findMy(User user, Pageable pageable) {
        Profile profile = findProfileOrThrow(user);
        return postRepository.findByProfile(profile, pageable).map(postMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PostResponse findById(User auth, UUID id) {
        Post post = findPostOrThrow(id);
        Profile postOwner = post.getProfile();

        if (postOwner.isPrivate()) {
            Profile requester = findProfileOrThrow(auth);
            boolean isOwner = requester.getId().equals(postOwner.getId());

            if (!isOwner && !followRepository.existsByFollowerAndFollowing(requester, postOwner)) {
                throw new ForbiddenException("This post belongs to a private profile");
            }
        }

        return postMapper.toResponse(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> findByProfile(User auth, UUID profileId, Pageable pageable) {
        Profile targetProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + profileId));

        if (targetProfile.isPrivate()) {
            Profile requester = findProfileOrThrow(auth);
            boolean isOwner = requester.getId().equals(targetProfile.getId());

            if (!isOwner && !followRepository.existsByFollowerAndFollowing(requester, targetProfile)) {
                throw new ForbiddenException("This profile is private");
            }
        }

        return postRepository.findByProfile(targetProfile, pageable).map(postMapper::toResponse);
    }

    @Transactional
    public PostResponse create(User user, CreatePostRequest request) {
        Profile profile = findProfileOrThrow(user);
        Post post = postMapper.toEntity(request);
        post.setProfile(profile);
        return postMapper.toResponse(postRepository.save(post));
    }

    @Transactional
    public PostResponse update(UUID postId, User auth, UpdatePostRequest request) {
        Post post = findPostOrThrow(postId);
        validateOwnerProfile(findProfileOrThrow(auth), post);
        post.setContent(request.content());
        return postMapper.toResponse(postRepository.save(post));
    }

    @Transactional
    public void delete(User auth, UUID id) {
        validateOwnerProfile(findProfileOrThrow(auth), findPostOrThrow(id));
        postRepository.deleteById(id);
    }

    private void validateOwnerProfile(Profile profile, Post post) {
        if (!profile.getId().equals(post.getProfile().getId())) {
            throw new ForbiddenException("You cannot modify this post");
        }
    }

    private Profile findProfileOrThrow(User user) {
        return profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + user.getId()));
    }

    private Post findPostOrThrow(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }
}