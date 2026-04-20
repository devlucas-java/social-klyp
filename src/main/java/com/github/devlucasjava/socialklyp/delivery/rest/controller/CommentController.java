package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.request.comment.CreateCommentRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.comment.UpdateCommentRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.comment.CommentResponse;
import com.github.devlucasjava.socialklyp.application.service.CommentService;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
@Tag(name = "Comments", description = "Comment management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    @Operation(summary = "List all comments of a post")
    public ResponseEntity<Page<CommentResponse>> findAllByPost(
            @PathVariable UUID postId,
            Pageable pageable) {
        return ResponseEntity.ok(commentService.findAllByPost(postId, pageable));
    }

    @PostMapping
    @Operation(summary = "Add a comment to a post")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User auth,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = commentService.addComment(postId, auth, request);
        URI location = URI.create("/posts/" + postId + "/comments/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Update a comment")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal User auth,
            @Valid @RequestBody UpdateCommentRequest request) {
        return ResponseEntity.ok(commentService.updateComment(commentId, auth, request));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal User auth) {
        commentService.deleteComment(commentId, auth);
        return ResponseEntity.noContent().build();
    }
}
