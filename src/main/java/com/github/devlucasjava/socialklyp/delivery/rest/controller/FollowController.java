package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.response.follow.FollowResponse;
import com.github.devlucasjava.socialklyp.application.dto.response.follow.FollowStatsResponse;
import com.github.devlucasjava.socialklyp.application.service.FollowService;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Follows", description = "Follow/Unfollow management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{profileId}")
    @Operation(summary = "Follow a profile")
    public ResponseEntity<FollowResponse> follow(
            @AuthenticationPrincipal User user,
            @PathVariable UUID profileId) {
        FollowResponse response = followService.follow(user, profileId);
        URI location = URI.create("/follows/" + profileId);
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{profileId}")
    @Operation(summary = "Unfollow a profile")
    public ResponseEntity<Void> unfollow(
            @AuthenticationPrincipal User user,
            @PathVariable UUID profileId) {
        followService.unfollow(user, profileId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{profileId}/followers")
    @Operation(summary = "List followers of a profile")
    public ResponseEntity<Page<FollowResponse>> listFollowers(
            @PathVariable UUID profileId,
            Pageable pageable) {
        return ResponseEntity.ok(followService.listFollowers(profileId, pageable));
    }

    @GetMapping("/{profileId}/following")
    @Operation(summary = "List profiles that a profile is following")
    public ResponseEntity<Page<FollowResponse>> listFollowing(
            @PathVariable UUID profileId,
            Pageable pageable) {
        return ResponseEntity.ok(followService.listFollowing(profileId, pageable));
    }

    @GetMapping("/{profileId}/stats")
    @Operation(summary = "Get follow stats (followers/following count) for a profile")
    public ResponseEntity<FollowStatsResponse> getStats(@PathVariable UUID profileId) {
        return ResponseEntity.ok(followService.getStats(profileId));
    }

    @GetMapping("/{profileId}/is-following")
    @Operation(summary = "Check if the authenticated user follows a profile")
    public ResponseEntity<Boolean> isFollowing(
            @AuthenticationPrincipal User user,
            @PathVariable UUID profileId) {
        return ResponseEntity.ok(followService.isFollowing(user, profileId));
    }
}
