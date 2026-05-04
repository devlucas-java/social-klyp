package com.github.devlucasjava.socialklyp.application.dto.response.message;

import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Message data returned by the API")
public class MessageResponse {

    @Schema(description = "Message unique identifier")
    private UUID id;

    @Schema(description = "ID of the chat this message belongs to")
    private UUID chatId;

    @Schema(description = "Message content (null if deleted)")
    private String content;

    @Schema(description = "Profile who sent the message")
    private ProfileSummary sender;

    @Schema(description = "Whether the message was deleted")
    private boolean deleted;

    @Schema(description = "When the message was sent")
    private LocalDateTime createdAt;

    @Schema(description = "When the message was last edited")
    private LocalDateTime updatedAt;
}
