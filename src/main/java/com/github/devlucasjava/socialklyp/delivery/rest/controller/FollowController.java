package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.response.follow.FollowResponse;
import com.github.devlucasjava.socialklyp.application.dto.response.follow.FollowStatsResponse;
import com.github.devlucasjava.socialklyp.application.service.FollowService;
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
@RequestMapping("/follows")
@Tag(name = "Follows", description = "Follow and unfollow management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{profileId}")
    @Operation(summary = "Follow a profile", description = "Starts following the given profile")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Now following",
                    content = @Content(schema = @Schema(implementation = FollowResponse.class))),
            @ApiResponse(responseCode = "403", description = "Cannot follow yourself", content = @Content),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Already following", content = @Content)
    })
    public ResponseEntity<FollowResponse> follow(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Target profile UUID", required = true) @PathVariable UUID profileId) {
        FollowResponse response = followService.follow(user, profileId);
        URI location = URI.create("/follows/" + profileId);
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{profileId}")
    @Operation(summary = "Unfollow a profile", description = "Stops following the given profile")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Unfollowed successfully"),
            @ApiResponse(responseCode = "404", description = "Profile not found or not following", content = @Content)
    })
    public ResponseEntity<Void> unfollow(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Target profile UUID", required = true) @PathVariable UUID profileId) {
        followService.unfollow(user, profileId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{profileId}/followers")
    @Operation(summary = "List followers", description = "Returns a paginated list of profiles that follow the given profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Followers returned"),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content)
    })
    public ResponseEntity<Page<FollowResponse>> listFollowers(
            @Parameter(description = "Profile UUID", required = true) @PathVariable UUID profileId,
            Pageable pageable) {
        return ResponseEntity.ok(followService.listFollowers(profileId, pageable));
    }

    @GetMapping("/{profileId}/following")
    @Operation(summary = "List following", description = "Returns a paginated list of profiles that the given profile follows")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Following list returned"),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content)
    })
    public ResponseEntity<Page<FollowResponse>> listFollowing(
            @Parameter(description = "Profile UUID", required = true) @PathVariable UUID profileId,
            Pageable pageable) {
        return ResponseEntity.ok(followService.listFollowing(profileId, pageable));
    }

    @GetMapping("/{profileId}/stats")
    @Operation(summary = "Follow stats", description = "Returns the total followers and following count for a profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stats returned",
                    content = @Content(schema = @Schema(implementation = FollowStatsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content)
    })
    public ResponseEntity<FollowStatsResponse> getStats(
            @Parameter(description = "Profile UUID", required = true) @PathVariable UUID profileId) {
        return ResponseEntity.ok(followService.getStats(profileId));
    }

    @GetMapping("/{profileId}/is-following")
    @Operation(summary = "Check following", description = "Returns true if the authenticated user follows the given profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Result returned"),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content)
    })
    public ResponseEntity<Boolean> isFollowing(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Target profile UUID", required = true) @PathVariable UUID profileId) {
        return ResponseEntity.ok(followService.isFollowing(user, profileId));
    }
}
