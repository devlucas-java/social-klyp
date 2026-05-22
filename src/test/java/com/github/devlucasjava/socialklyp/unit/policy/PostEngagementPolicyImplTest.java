package com.github.devlucasjava.socialklyp.unit.policy;

import com.github.devlucasjava.socialklyp.domain.entity.Follow;
import com.github.devlucasjava.socialklyp.domain.entity.Like;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.policy.impl.PostEngagementPolicyImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class PostEngagementPolicyImplTest {

    private final PostEngagementPolicyImpl policy = new PostEngagementPolicyImpl();

    @Test
    void shouldReturnFalseWhenPostOrProfileIsNull() {
        assertFalse(policy.hasAlreadyLiked(null, null));
        assertFalse(policy.canLikePost(null, null));
        assertFalse(policy.canUnlikePost(null, null));
        assertFalse(policy.canCommentOnPost(null, null));
    }

    @Test
    void shouldHandleLikesCorrectly() {
        Profile profile = createProfile("profile-1");
        Post post = createPost(profile);

        assertTrue(policy.hasAlreadyLiked(profile, post));
        assertFalse(policy.canLikePost(profile, post));
        assertTrue(policy.canUnlikePost(profile, post));
    }

    @Test
    void shouldAllowLikeWhenNotAlreadyLiked() {
        Profile profile = createProfile("profile-1");
        Post post = new Post();
        post.setLikes(new HashSet<>());

        assertTrue(policy.canLikePost(profile, post));
        assertFalse(policy.canUnlikePost(profile, post));
    }

    @Test
    void shouldAllowCommentOnPublicPostForAnyone() {
        Profile owner = createProfile(false);
        Post post = new Post();
        post.setProfile(owner);

        assertTrue(policy.canCommentOnPost(null, post));
        assertTrue(policy.canCommentOnPost(createProfile(false), post));
    }

    @Test
    void shouldAllowCommentOnPrivatePostForFollowerAndOwner() {
        Profile owner = createProfile(true);
        Profile follower = createProfile(false);
        follower.setFollowing(new HashSet<>());

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(owner);
        follower.getFollowing().add(follow);

        Post post = new Post();
        post.setProfile(owner);

        assertTrue(policy.canCommentOnPost(owner, post));
        assertTrue(policy.canCommentOnPost(follower, post));
    }

    private Profile createProfile(String idSuffix) {
        Profile profile = new Profile();
        profile.setId(java.util.UUID.nameUUIDFromBytes(idSuffix.getBytes()));
        profile.setDisplayName(idSuffix);
        profile.setBio("Bio");
        profile.setPrivate(false);
        return profile;
    }

    private Profile createProfile(boolean isPrivate) {
        Profile profile = new Profile();
        profile.setId(java.util.UUID.randomUUID());
        profile.setDisplayName(isPrivate ? "private" : "public");
        profile.setBio("Bio");
        profile.setPrivate(isPrivate);
        profile.setFollowing(new HashSet<>());
        return profile;
    }

    private Post createPost(Profile likedBy) {
        Post post = new Post();
        Like like = new Like();
        like.setProfile(likedBy);
        post.setLikes(new HashSet<>());
        post.getLikes().add(like);
        return post;
    }
}
