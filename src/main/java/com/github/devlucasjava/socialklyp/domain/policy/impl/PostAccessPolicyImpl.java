package com.github.devlucasjava.socialklyp.domain.policy.impl;

import com.github.devlucasjava.socialklyp.delivery.rest.advice.ForbiddenException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.policy.PostAccessPolicy;
import org.springframework.stereotype.Service;

@Service
public class PostAccessPolicyImpl implements PostAccessPolicy {

    @Override
    public boolean canViewPost(Post post, Profile requester) {

        validatePost(post);

        Profile owner = post.getProfile();

        if (owner == null) {
            throw new ResourceNotFoundException("Post owner not found");
        }

        if (isPostOwner(post, requester)) {
            return true;
        }

        if (owner.isPrivate()) {

            if (requester == null) {
                throw new ForbiddenException("Private profile");
            }

            if (!requester.isFollowing(owner)) {
                throw new ForbiddenException("You do not follow this profile");
            }
        }

        return true;
    }

    @Override
    public boolean canViewProfilePosts(Profile postOwner, Profile requester) {

        validateProfile(postOwner);

        if (requester != null && requester.isSameAs(postOwner)) {
            return true;
        }

        if (postOwner.isPrivate()) {

            if (requester == null) {
                throw new ForbiddenException("Private profile");
            }

            if (!requester.isFollowing(postOwner)) {
                throw new ForbiddenException("You do not follow this profile");
            }
        }

        return true;
    }

    @Override
    public boolean isPostOwner(Post post, Profile requester) {

        validatePost(post);

        if (requester == null) {
            return false;
        }

        return post.isOwnedBy(requester);
    }

    private void validatePost(Post post) {
        if (post == null) {
            throw new ResourceNotFoundException("Post not found");
        }
    }

    private void validateProfile(Profile profile) {
        if (profile == null) {
            throw new ResourceNotFoundException("Profile not found");
        }
    }
}