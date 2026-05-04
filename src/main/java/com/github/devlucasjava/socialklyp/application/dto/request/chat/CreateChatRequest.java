package com.github.devlucasjava.socialklyp.application.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateChatRequest {

    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
}
