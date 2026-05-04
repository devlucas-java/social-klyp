package com.github.devlucasjava.socialklyp.application.dto.response.chat;

import com.github.devlucasjava.socialklyp.application.dto.response.link.LinkResponse;
import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileSummary;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ChatResponse {

    private UUID id;

    private String name;

    private String description;

    private List<ProfileSummary> profiles;

    private List<ProfileSummary> profilesAdmin;

    private List<LinkResponse> links;

    private ProfileSummary profileCreator;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
