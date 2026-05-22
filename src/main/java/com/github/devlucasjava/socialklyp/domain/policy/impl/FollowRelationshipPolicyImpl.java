package com.github.devlucasjava.socialklyp.domain.policy.impl;

import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.policy.FollowRelationshipPolicy;
import org.springframework.stereotype.Service;

@Service
public class FollowRelationshipPolicyImpl implements FollowRelationshipPolicy {

    @Override
    public boolean isAlreadyFollowing(Profile follower, Profile targetProfile) {
        return follower != null
                && targetProfile != null
                && follower.isFollowing(targetProfile);
    }

    @Override
    public boolean areDifferentProfiles(Profile follower, Profile targetProfile) {
        return follower != null
                && targetProfile != null
                && !follower.isSameAs(targetProfile);
    }
}