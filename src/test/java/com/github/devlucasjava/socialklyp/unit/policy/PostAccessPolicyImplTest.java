package com.github.devlucasjava.socialklyp.unit.policy;

import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.policy.impl.PostAccessPolicyImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class PostAccessPolicyImplTest {

    private final PostAccessPolicyImpl policy = new PostAccessPolicyImpl();

    @Test
    void shouldAllowAnonymousToViewPublicPost() {
        Profile publicProfile = createProfile(false);
        Post post = new Post();
        post.setProfile(publicProfile);

        assertTrue(policy.canViewPost(post, null));
    }

    @Test
    void shouldDenyAnonymousOnPrivatePost() {
        Profile privateProfile = createProfile(true);
        Post post = new Post();
        post.setProfile(privateProfile);

        assertFalse(policy.canViewPost(post, null));
    }

    @Test
    void shouldAllowOwnerToViewOwnPrivatePost() {
        Profile owner = createProfile(true);
        Post post = new Post();
        post.setProfile(owner);

        assertTrue(policy.canViewPost(post, owner));
    }

    @Test
    void shouldAllowFollowerToViewPrivatePost() {
        Profile owner = createProfile(true);
        Profile follower = createProfile(false);
        follower.setFollowing(new java.util.HashSet<>());
        com.github.devlucasjava.socialklyp.domain.entity.Follow follow = new com.github.devlucasjava.socialklyp.domain.entity.Follow();
        follow.setFollower(follower);
        follow.setFollowing(owner);
        follower.getFollowing().add(follow);

        Post post = new Post();
        post.setProfile(owner);

        assertTrue(policy.canViewPost(post, follower));
    }

    @Test
    void shouldAllowRequestorToViewTheirOwnProfilePosts() {
        Profile owner = createProfile(true);
        assertTrue(policy.canViewProfilePosts(owner, owner));
    }

    @Test
    void shouldDenyRequestorToViewPrivateProfileWhenNotFollowing() {
        Profile owner = createProfile(true);
        Profile other = createProfile(false);

        assertFalse(policy.canViewProfilePosts(owner, other));
    }

    @Test
    void shouldAllowRequestorToViewPublicProfilePosts() {
        Profile owner = createProfile(false);
        Profile other = createProfile(false);

        assertTrue(policy.canViewProfilePosts(owner, other));
    }

    @Test
    void shouldIdentifyPostOwner() {
        Profile owner = createProfile(false);
        Post post = new Post();
        post.setProfile(owner);

        assertTrue(policy.isPostOwner(post, owner));
        assertFalse(policy.isPostOwner(post, createProfile(false)));
    }

    private Profile createProfile(boolean isPrivate) {
        Profile profile = new Profile();
        profile.setId(java.util.UUID.randomUUID());
        profile.setDisplayName("profile");
        profile.setBio("bio");
        profile.setPrivate(isPrivate);
        return profile;
    }
}
