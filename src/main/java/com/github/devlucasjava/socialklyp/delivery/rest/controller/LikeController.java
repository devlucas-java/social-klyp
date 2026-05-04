package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.response.like.LikeResponse;
import com.github.devlucasjava.socialklyp.application.dto.response.utils.BooleanDTO;
import com.github.devlucasjava.socialklyp.application.service.LikeService;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Count likes", description = "Returns the total number of likes on a post")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Like count returned"),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
    })
    public ResponseEntity<Long> countLikes(
            @Parameter(description = "Post UUID", required = true) @PathVariable UUID postId) {
        return ResponseEntity.ok(likeService.countLikesByPost(postId));
    }

    @GetMapping
    @Operation(summary = "Check if liked", description = "Returns whether the authenticated user has liked the post")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Result returned",
                    content = @Content(schema = @Schema(implementation = BooleanDTO.class))),
            @ApiResponse(responseCode = "404", description = "Post or profile not found", content = @Content)
    })
    public ResponseEntity<BooleanDTO> hasLikePost(
            @Parameter(description = "Post UUID", required = true) @PathVariable UUID postId,
            @AuthenticationPrincipal User auth) {
        return ResponseEntity.ok(likeService.hasLikePost(postId, auth));
    }

    @PostMapping
    @Operation(summary = "Like post", description = "Likes a post (each user can like a post only once)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post liked",
                    content = @Content(schema = @Schema(implementation = LikeResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post or profile not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Post already liked", content = @Content)
    })
    public ResponseEntity<LikeResponse> likePost(
            @Parameter(description = "Post UUID", required = true) @PathVariable UUID postId,
            @AuthenticationPrincipal User auth) {
        return ResponseEntity.ok(likeService.likePost(postId, auth));
    }

    @DeleteMapping
    @Operation(summary = "Unlike post", description = "Removes the like from a post")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Like removed"),
            @ApiResponse(responseCode = "404", description = "Like, post or profile not found", content = @Content)
    })
    public ResponseEntity<Void> unlikePost(
            @Parameter(description = "Post UUID", required = true) @PathVariable UUID postId,
            @AuthenticationPrincipal User auth) {
        likeService.unlikePost(postId, auth);
        return ResponseEntity.noContent().build();
    }
}
