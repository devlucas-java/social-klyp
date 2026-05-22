package com.github.devlucasjava.socialklyp.domain.policy;

import com.github.devlucasjava.socialklyp.domain.entity.Profile;

/**
 * Domain Service for follow relationship logic.
 * Encapsulates business rules for following/unfollowing between profiles.
 */
public interface FollowRelationshipPolicy {

    /**
     * Checks if a profile is already following another profile.
     * @param follower the follower profile
     * @param targetProfile the target profile
     * @return true if already following
     */
    boolean isAlreadyFollowing(Profile follower, Profile targetProfile);

    /**
     * Checks if follower and targetProfile are different profiles.
     * @param follower the follower profile
     * @param targetProfile the target profile
     * @return true if they are different profiles
     */
    boolean areDifferentProfiles(Profile follower, Profile targetProfile);
}
