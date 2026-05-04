package com.github.devlucasjava.socialklyp.application.dto.request.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateChatRequest {

    private String name;
    private String description;
}
