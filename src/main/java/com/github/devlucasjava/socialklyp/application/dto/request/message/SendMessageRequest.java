package com.github.devlucasjava.socialklyp.application.dto.request.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to send a message in a chat")
public record SendMessageRequest(

        @Schema(description = "Message content", example = "Hello everyone!")
        @NotBlank(message = "Content must not be blank")
        @Size(max = 2000, message = "Message must not exceed 2000 characters")
        String content
) {}
