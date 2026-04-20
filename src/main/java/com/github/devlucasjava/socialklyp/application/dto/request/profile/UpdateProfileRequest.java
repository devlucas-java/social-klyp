package com.github.devlucasjava.socialklyp.application.dto.request.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to update an existing profile")
public record UpdateProfileRequest(

        @Schema(description = "Updated display name", example = "Jane Doe")
        @Size(max = 100, message = "Display name must not exceed 100 characters")
        String displayName,

        @Schema(description = "Updated bio", example = "Now a full-stack dev!")
        @Size(max = 300, message = "Bio must not exceed 300 characters")
        String bio,

        @Schema(description = "Whether the profile is private")
        Boolean isPrivate
) {}
