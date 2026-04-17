package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.request.comment.CreateCommentRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.comment.UpdateCommentRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.comment.CommentResponse;
import com.github.devlucasjava.socialklyp.application.mapper.CommentMapper;
import com.github.devlucasjava.socialklyp.domain.entity.Comment;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.CommentRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.PostRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    public Page<CommentResponse> findAllByPost(UUID postId, Pageable pageable) {
        findPostOrThrow(postId);
        return commentRepository.findAllByPostId(postId, pageable).map(commentMapper::toResponse);
    }

    @Transactional
    public CommentResponse addComment(UUID postId, UUID profileId, CreateCommentRequest request) {
        Post post = findPostOrThrow(postId);
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + profileId));

        Comment comment = new Comment();
        comment.setContent(request.content());
        comment.setProfile(profile);

        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponse updateComment(UUID commentId, UUID profileId, UpdateCommentRequest request) {
        Comment comment = findCommentOrThrow(commentId);
        validateCommentOwnership(comment, profileId);
        comment.setContent(request.content());
        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID profileId) {
        Comment comment = findCommentOrThrow(commentId);
        validateCommentOwnership(comment, profileId);
        commentRepository.delete(comment);
    }

    private void validateCommentOwnership(Comment comment, UUID profileId) {
        if (!comment.getProfile().getId().equals(profileId)) {
            throw new IllegalStateException("Profile does not own this comment");
        }
    }

    private Comment findCommentOrThrow(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
    }

    private Post findPostOrThrow(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
    }
}
