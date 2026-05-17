package com.github.devlucasjava.socialklyp.application.dto.response.follow;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Follow statistics for a profile")
public record FollowStatsResponse(

        @Schema(description = "Number of followers")
        long followers,

        @Schema(description = "Number of profiles being followed")
        long following
) {}
