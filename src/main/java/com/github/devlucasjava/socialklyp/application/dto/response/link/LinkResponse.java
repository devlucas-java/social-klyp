package com.github.devlucasjava.socialklyp.application.dto.response.link;

import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Invite link data returned by the API")
public class LinkResponse {

    @Schema(description = "Link unique identifier")
    private UUID id;

    @Schema(description = "ID of the chat this link belongs to")
    private UUID chatId;

    @Schema(description = "Whether the link is still active")
    private boolean active;

    @Schema(description = "Number of users who joined via this link")
    private long usersSubscribed;

    @Schema(description = "Profile that created the link")
    private ProfileSummary profileCreate;

    @Schema(description = "When the link was created")
    private LocalDateTime createdAt;

    @Schema(description = "When the link expires (null = never)")
    private LocalDateTime expirationDate;
}
