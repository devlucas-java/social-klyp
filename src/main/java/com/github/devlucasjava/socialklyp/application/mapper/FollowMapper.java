package com.github.devlucasjava.socialklyp.application.mapper;

import com.github.devlucasjava.socialklyp.application.dto.response.follow.FollowResponse;
import com.github.devlucasjava.socialklyp.domain.entity.Follow;
import org.springframework.stereotype.Component;

@Component
public class FollowMapper {

    public FollowResponse toResponse(Follow follow) {
        return new FollowResponse(
                follow.getId(),
                follow.getFollower().getId(),
                follow.getFollower().getDisplayName(),
                follow.getFollower().getProfilePictureUrl(),
                follow.getFollowing().getId(),
                follow.getFollowing().getDisplayName(),
                follow.getFollowing().getProfilePictureUrl(),
                follow.getCreatedAt()
        );
    }
}
