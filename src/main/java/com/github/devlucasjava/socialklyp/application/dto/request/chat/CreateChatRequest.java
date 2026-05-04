package com.github.devlucasjava.socialklyp.application.dto.request.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Payload to create a new chat")
public class CreateChatRequest {

    @Schema(description = "Chat name", example = "Dev Team")
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Schema(description = "Chat description (optional)", example = "Channel for the dev team")
    @Size(max = 3000, message = "Description must not exceed 3000 characters")
    private String description;
}
