package com.github.devlucasjava.socialklyp.application.dto.request.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Payload to update an existing chat")
public class UpdateChatRequest {

    @Schema(description = "Updated chat name", example = "Backend Team")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Schema(description = "Updated chat description", example = "Only backend devs here")
    @Size(max = 3000, message = "Description must not exceed 3000 characters")
    private String description;
}
