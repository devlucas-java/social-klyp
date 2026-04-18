package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.response.like.LikeResponse;
import com.github.devlucasjava.socialklyp.application.service.LikeService;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/likes")
@Tag(name = "Likes", description = "Like management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/count")
    @Operation(summary = "Count likes on a post")
    public ResponseEntity<Long> countLikes(@PathVariable UUID postId) {
        return ResponseEntity.ok(likeService.countLikesByPost(postId));
    }

    @PostMapping
    @Operation(summary = "Like a post")
    public ResponseEntity<LikeResponse> likePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(likeService.likePost(postId, user.getProfile().getId()));
    }

    @DeleteMapping
    @Operation(summary = "Unlike a post")
    public ResponseEntity<Void> unlikePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User user) {
        likeService.unlikePost(postId, user.getProfile().getId());
        return ResponseEntity.noContent().build();
    }
}
