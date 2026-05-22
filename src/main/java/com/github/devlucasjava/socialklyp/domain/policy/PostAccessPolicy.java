package com.github.devlucasjava.socialklyp.domain.policy;

import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;

/**
 * Domain Service for post access control logic.
 * Encapsulates business rules for determining if a profile can view a post.
 * 
 * This is a Domain Service (not an Application Service) because it handles
 * cross-aggregate concerns and pure domain logic that should not depend
 * on persistence or HTTP layers.
 */
public interface PostAccessPolicy {

    /**
     * Determines if a requester can view a post.
     * 
     * Rules:
     * - Owner can always view their own posts
     * - If post owner's profile is private, only followers/owner can view
     * - Public posts can be viewed by anyone
     * 
     * @param post the post to check access for
     * @param requester the profile requesting access (null means anonymous)
     * @return true if access is allowed, false otherwise
     */
    boolean canViewPost(Post post, Profile requester);

    /**
     * Determines if a requester can view posts from a profile.
     * 
     * @param postOwner the profile that owns the posts
     * @param requester the profile requesting access (null means anonymous)
     * @return true if access is allowed, false otherwise
     */
    boolean canViewProfilePosts(Profile postOwner, Profile requester);

    /**
     * Checks if requester is the post owner.
     */
    boolean isPostOwner(Post post, Profile requester);
}
