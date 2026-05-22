package com.github.devlucasjava.socialklyp.unit.policy;

import com.github.devlucasjava.socialklyp.domain.entity.Follow;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.policy.impl.FollowRelationshipPolicyImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class FollowRelationshipPolicyImplTest {

    private final FollowRelationshipPolicyImpl policy = new FollowRelationshipPolicyImpl();

    @Test
    void shouldAllowFollowingDifferentProfileWhenNotAlreadyFollowing() {
        Profile follower = createProfile("follower");
        Profile target = createProfile("target");

        assertTrue(policy.canFollow(follower, target));
    }

    @Test
    void shouldNotAllowFollowingSelf() {
        Profile profile = createProfile("self");

        assertFalse(policy.canFollow(profile, profile));
        assertFalse(policy.areDifferentProfiles(profile, profile));
    }

    @Test
    void shouldNotAllowFollowingWhenAlreadyFollowing() {
        Profile follower = createProfile("follower");
        Profile target = createProfile("target");
        follower.setFollowing(new HashSet<>());
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(target);
        follower.getFollowing().add(follow);

        assertTrue(policy.isAlreadyFollowing(follower, target));
        assertFalse(policy.canFollow(follower, target));
    }

    @Test
    void shouldTreatNullInputsAsNotFollowingAndNotAllowed() {
        assertFalse(policy.canFollow(null, null));
        assertFalse(policy.isAlreadyFollowing(null, null));
        assertFalse(policy.areDifferentProfiles(null, null));
    }

    private Profile createProfile(String name) {
        Profile profile = new Profile();
        profile.setId(java.util.UUID.nameUUIDFromBytes(name.getBytes()));
        profile.setDisplayName(name);
        profile.setBio("Bio");
        profile.setFollowing(new HashSet<>());
        return profile;
    }
}
