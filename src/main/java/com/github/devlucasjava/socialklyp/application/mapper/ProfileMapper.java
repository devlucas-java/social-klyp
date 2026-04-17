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
                profile.getProfilePictureUrl()
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

    public Profile toEntity(CreateProfileRequest request) {

        Profile profile = new Profile();
        profile.setDisplayName(request.displayName());
        profile.setBio(request.bio());
        profile.setPrivate(request.isPrivate());
        return profile;
    }
}
