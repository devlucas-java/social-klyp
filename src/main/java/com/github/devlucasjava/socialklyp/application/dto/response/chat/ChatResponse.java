package com.github.devlucasjava.socialklyp.application.dto.response.chat;

import com.github.devlucasjava.socialklyp.application.dto.response.link.LinkResponse;
import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Chat data returned by the API")
public class ChatResponse {

    @Schema(description = "Chat unique identifier")
    private UUID id;

    @Schema(description = "Chat name", example = "Dev Team")
    private String name;

    @Schema(description = "Chat description")
    private String description;

    @Schema(description = "Regular members of the chat")
    private List<ProfileSummary> profiles;

    @Schema(description = "Admin members of the chat")
    private List<ProfileSummary> profilesAdmin;

    @Schema(description = "Active invite links")
    private List<LinkResponse> links;

    @Schema(description = "Profile that created the chat")
    private ProfileSummary profileCreator;

    @Schema(description = "Chat creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Chat last update timestamp")
    private LocalDateTime updatedAt;
}
