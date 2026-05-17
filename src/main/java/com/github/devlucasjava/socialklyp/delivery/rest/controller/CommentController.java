package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.request.comment.CreateCommentRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.comment.UpdateCommentRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.comment.CommentResponse;
import com.github.devlucasjava.socialklyp.application.service.CommentService;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "List comments", description = "Returns all comments of a post, paginated")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comments returned"),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
    })
    public ResponseEntity<Page<CommentResponse>> findAllByPost(
            @Parameter(description = "Post UUID", required = true) @PathVariable UUID postId,
            Pageable pageable) {
        return ResponseEntity.ok(commentService.findAllByPost(postId, pageable));
    }

    @PostMapping
    @Operation(summary = "Add comment", description = "Adds a comment to a post")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment created",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post or profile not found", content = @Content)
    })
    public ResponseEntity<CommentResponse> addComment(
            @Parameter(description = "Post UUID", required = true) @PathVariable UUID postId,
            @AuthenticationPrincipal User auth,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = commentService.addComment(postId, auth, request);
        URI location = URI.create("/posts/" + postId + "/comments/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Update comment", description = "Updates a comment's content (owner only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment updated",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Not the comment owner", content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment or post not found", content = @Content)
    })
    public ResponseEntity<CommentResponse> updateComment(
            @Parameter(description = "Comment UUID", required = true) @PathVariable UUID commentId,
            @Parameter(description = "Post UUID", required = true) @PathVariable UUID postId,
            @AuthenticationPrincipal User auth,
            @Valid @RequestBody UpdateCommentRequest request) {
        return ResponseEntity.ok(commentService.updateComment(postId, commentId, auth, request));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete comment", description = "Deletes a comment (owner only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comment deleted"),
            @ApiResponse(responseCode = "401", description = "Not the comment owner", content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment or post not found", content = @Content)
    })
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "Comment UUID", required = true) @PathVariable UUID commentId,
            @Parameter(description = "Post UUID", required = true) @PathVariable UUID postId,
            @AuthenticationPrincipal User auth) {
        commentService.deleteComment(postId, commentId, auth);
        return ResponseEntity.noContent().build();
    }
}
