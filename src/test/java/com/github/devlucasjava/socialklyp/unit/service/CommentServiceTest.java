package com.github.devlucasjava.socialklyp.unit.service;

import com.github.devlucasjava.socialklyp.application.dto.request.comment.CreateCommentRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.comment.UpdateCommentRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.comment.CommentResponse;
import com.github.devlucasjava.socialklyp.application.mapper.CommentMapper;
import com.github.devlucasjava.socialklyp.application.service.CommentService;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.UnauthorizeException;
import com.github.devlucasjava.socialklyp.domain.entity.Comment;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.CommentRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.PostRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private CommentMapper commentMapper;

    private User authUser;
    private Profile profile;
    private Post post;
    private Comment comment;
    private CommentResponse commentResponse;

    private final UUID postId = UUID.randomUUID();
    private final UUID commentId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        authUser = new User();
        authUser.setId(userId);

        profile = new Profile();
        profile.setId(profileId);
        profile.setDisplayName("Test User");
        profile.setUser(authUser);

        post = new Post();
        post.setId(postId);
        post.setContent("Post content");
        post.setProfile(profile);

        comment = new Comment();
        comment.setId(commentId);
        comment.setContent("Test comment");
        comment.setProfile(profile);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        commentResponse = new CommentResponse(commentId, "Test comment", null, comment.getCreatedAt(), comment.getUpdatedAt());
    }

    // -------------------- FIND ALL BY POST --------------------

    @Test
    void shouldFindAllCommentsByPost() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> page = new PageImpl<>(List.of(comment));

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findAllByPostId(postId, pageable)).thenReturn(page);
        when(commentMapper.toResponse(comment)).thenReturn(commentResponse);

        Page<CommentResponse> result = commentService.findAllByPost(postId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(commentId, result.getContent().get(0).id());
    }

    @Test
    void shouldThrowWhenPostNotFoundOnFindAll() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.findAllByPost(postId, PageRequest.of(0, 10)));
    }

    // -------------------- ADD COMMENT --------------------

    @Test
    void shouldAddCommentSuccessfully() {
        CreateCommentRequest request = new CreateCommentRequest("Great post!");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(profile));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(commentResponse);

        CommentResponse result = commentService.addComment(postId, authUser, request);

        assertNotNull(result);
        assertEquals(commentId, result.id());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldThrowWhenPostNotFoundOnAddComment() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.addComment(postId, authUser, new CreateCommentRequest("content")));

        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnAddComment() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> commentService.addComment(postId, authUser, new CreateCommentRequest("content")));

        verify(commentRepository, never()).save(any());
    }

    // -------------------- UPDATE COMMENT --------------------

    @Test
    void shouldUpdateCommentSuccessfully() {
        UpdateCommentRequest request = new UpdateCommentRequest("Updated content");
        CommentResponse updatedResponse = new CommentResponse(commentId, "Updated content", null, comment.getCreatedAt(), comment.getUpdatedAt());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.existsByIdAndPostId(commentId, postId)).thenReturn(true);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toResponse(comment)).thenReturn(updatedResponse);

        CommentResponse result = commentService.updateComment(postId, commentId, authUser, request);

        assertNotNull(result);
        assertEquals("Updated content", result.content());
        verify(commentRepository).save(comment);
    }

    @Test
    void shouldThrowWhenCommentNotFoundOnUpdate() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.updateComment(postId, commentId, authUser, new UpdateCommentRequest("content")));
    }

    @Test
    void shouldThrowWhenUserDoesNotOwnCommentOnUpdate() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(UnauthorizeException.class,
                () -> commentService.updateComment(postId, commentId, otherUser, new UpdateCommentRequest("content")));
    }

    @Test
    void shouldThrowWhenCommentDoesNotBelongToPostOnUpdate() {
        UpdateCommentRequest request = new UpdateCommentRequest("Updated content");

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.existsByIdAndPostId(commentId, postId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.updateComment(postId, commentId, authUser, request));
    }

    // -------------------- DELETE COMMENT --------------------

    @Test
    void shouldDeleteCommentSuccessfully() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.existsByIdAndPostId(commentId, postId)).thenReturn(true);

        commentService.deleteComment(postId, commentId, authUser);

        verify(commentRepository).delete(comment);
    }

    @Test
    void shouldThrowWhenCommentNotFoundOnDelete() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.deleteComment(postId, commentId, authUser));

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenPostNotFoundOnDelete() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.deleteComment(postId, commentId, authUser));

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenCommentDoesNotBelongToPostOnDelete() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.existsByIdAndPostId(commentId, postId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> commentService.deleteComment(postId, commentId, authUser));

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenUserDoesNotOwnCommentOnDelete() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.existsByIdAndPostId(commentId, postId)).thenReturn(true);

        assertThrows(UnauthorizeException.class,
                () -> commentService.deleteComment(postId, commentId, otherUser));

        verify(commentRepository, never()).delete(any());
    }
}
