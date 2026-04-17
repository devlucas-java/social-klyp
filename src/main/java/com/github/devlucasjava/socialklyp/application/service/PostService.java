package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.request.post.CreatePostRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.post.UpdatePostRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.post.PostResponse;
import com.github.devlucasjava.socialklyp.application.mapper.PostMapper;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
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
    private final PostMapper postMapper;

    @Transactional(readOnly = true)
    public Page<PostResponse> findAll(Pageable pageable) {
        return postRepository.findAll(pageable).map(postMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> findMy(User user, Pageable pageable) {
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + user.getId()));
        return postRepository.findByProfile(profile, pageable).map(postMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PostResponse findById(UUID id) {
        return postMapper.toResponse(findPostOrThrow(id));
    }

    @Transactional
    public PostResponse create(User user, CreatePostRequest request) {
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + user.getId()));

        Post post = postMapper.toEntity(request);
        post.setProfile(profile);

        return postMapper.toResponse(postRepository.save(post));
    }

    @Transactional
    public PostResponse update(UUID postId, UpdatePostRequest request) {
        Post post = findPostOrThrow(postId);
        post.setContent(request.content());
        return postMapper.toResponse(postRepository.save(post));
    }

    @Transactional
    public void delete(UUID id) {
        findPostOrThrow(id);
        postRepository.deleteById(id);
    }

    private Post findPostOrThrow(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }
}