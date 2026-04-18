package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.request.post.CreatePostRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.post.UpdatePostRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.post.PostResponse;
import com.github.devlucasjava.socialklyp.application.service.PostService;
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
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Post management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class PostController {

    private final PostService postService;

    @GetMapping
    @Operation(summary = "List all posts paginated")
    public ResponseEntity<Page<PostResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(postService.findAll(pageable));
    }

    @GetMapping("/me")
    @Operation(summary = "List posts of the authenticated user")
    public ResponseEntity<Page<PostResponse>> findMy(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        return ResponseEntity.ok(postService.findMy(user, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a post by ID")
    public ResponseEntity<PostResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new post")
    public ResponseEntity<PostResponse> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreatePostRequest request) {
        PostResponse response = postService.create(user, request);
        URI location = URI.create("/posts/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a post")
    public ResponseEntity<PostResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePostRequest request) {
        return ResponseEntity.ok(postService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a post")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
