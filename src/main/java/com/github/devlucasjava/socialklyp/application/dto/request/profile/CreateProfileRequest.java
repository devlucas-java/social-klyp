package com.github.devlucasjava.socialklyp.application.dto.request.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to create a user profile")
public record CreateProfileRequest(

        @Schema(description = "User's display name", example = "John Doe")
        @NotBlank(message = "Display name must not be blank")
        @Size(max = 100, message = "Display name must not exceed 100 characters")
        String displayName,

        @Schema(description = "Short bio for the profile", example = "Software developer from Brazil")
        @NotBlank(message = "Bio must not be blank")
        @Size(max = 300, message = "Bio must not exceed 300 characters")
        String bio,

        @Schema(description = "Whether the profile is private", example = "false")
        boolean isPrivate
) {}
