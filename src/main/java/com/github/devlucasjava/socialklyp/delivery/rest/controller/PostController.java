package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.request.post.CreatePostRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.post.UpdatePostRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.post.PostResponse;
import com.github.devlucasjava.socialklyp.application.service.PostService;
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
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Post management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class PostController {

    private final PostService postService;

    @GetMapping
    @Operation(summary = "List public posts", description = "Returns a paginated list of posts from public profiles only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posts returned successfully")
    })
    public ResponseEntity<Page<PostResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(postService.findAll(pageable));
    }

    @GetMapping("/me")
    @Operation(summary = "List my posts", description = "Returns all posts created by the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posts returned"),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content)
    })
    public ResponseEntity<Page<PostResponse>> findMy(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return ResponseEntity.ok(postService.findMy(user, pageable));
    }

    @GetMapping("/profile/{profileId}")
    @Operation(
            summary = "List posts by profile",
            description = "Returns posts of a given profile. Private profiles require the requester to be a follower"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posts returned"),
            @ApiResponse(responseCode = "403", description = "Profile is private and requester is not a follower", content = @Content),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content)
    })
    public ResponseEntity<Page<PostResponse>> findByProfile(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Profile UUID", required = true) @PathVariable UUID profileId,
            Pageable pageable) {
        return ResponseEntity.ok(postService.findByProfile(user, profileId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get post by ID",
            description = "Returns a post. Private profile posts require the requester to be a follower"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post found",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "403", description = "Profile is private and requester is not a follower", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
    })
    public ResponseEntity<PostResponse> findById(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Post UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(postService.findById(user, id));
    }

    @PostMapping
    @Operation(summary = "Create post", description = "Creates a new post for the authenticated user's profile")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post created",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content)
    })
    public ResponseEntity<PostResponse> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreatePostRequest request) {
        PostResponse response = postService.create(user, request);
        URI location = URI.create("/posts/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update post", description = "Updates the content of a post (owner only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post updated",
                    content = @Content(schema = @Schema(implementation = PostResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not the post owner", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
    })
    public ResponseEntity<PostResponse> update(
            @Parameter(description = "Post UUID", required = true) @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdatePostRequest request) {
        return ResponseEntity.ok(postService.update(id, user, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete post", description = "Deletes a post (owner only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post deleted"),
            @ApiResponse(responseCode = "403", description = "Not the post owner", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
    })
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Post UUID", required = true) @PathVariable UUID id) {
        postService.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
