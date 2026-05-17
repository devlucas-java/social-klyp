package com.github.devlucasjava.socialklyp.application.dto.response.follow;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Follow relationship data")
public record FollowResponse(

        @Schema(description = "Follow relationship ID")
        UUID id,

        @Schema(description = "ID of the follower profile")
        UUID followerId,

        @Schema(description = "Display name of the follower")
        String followerDisplayName,

        @Schema(description = "Profile picture URL of the follower")
        String followerPictureUrl,

        @Schema(description = "ID of the followed profile")
        UUID followingId,

        @Schema(description = "Display name of the followed profile")
        String followingDisplayName,

        @Schema(description = "Profile picture URL of the followed profile")
        String followingPictureUrl,

        @Schema(description = "When the follow happened")
        LocalDateTime createdAt
) {}
