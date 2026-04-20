package com.github.devlucasjava.socialklyp.application.mapper;

import com.github.devlucasjava.socialklyp.application.dto.request.profile.CreateProfileRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileResponse;
import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileSummary;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public ProfileSummary toSummary(Profile profile) {
        return new ProfileSummary(
                profile.getId(),
                profile.getDisplayName(),
                profile.getProfilePictureUrl(),
                profile.isPrivate()
        );
    }

    public ProfileResponse toResponse(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getProfilePictureUrl(),
                profile.isPrivate(),
                profile.getUser().getId()
        );
    }
}
