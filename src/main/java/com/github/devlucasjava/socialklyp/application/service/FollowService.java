package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.response.follow.FollowResponse;
import com.github.devlucasjava.socialklyp.application.dto.response.follow.FollowStatsResponse;
import com.github.devlucasjava.socialklyp.application.mapper.FollowMapper;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ConflictException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ForbiddenException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Follow;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.FollowRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final ProfileRepository profileRepository;
    private final FollowMapper followMapper;

    @Transactional
    public FollowResponse follow(User auth, UUID targetProfileId) {
        Profile follower = findProfileByUserOrThrow(auth);
        Profile following = findProfileByIdOrThrow(targetProfileId);

        if (follower.getId().equals(following.getId())) {
            throw new ForbiddenException("You cannot follow yourself");
        }

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new ConflictException("You are already following this profile");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);

        return followMapper.toResponse(followRepository.save(follow));
    }

    @Transactional
    public void unfollow(User auth, UUID targetProfileId) {
        Profile follower = findProfileByUserOrThrow(auth);
        Profile following = findProfileByIdOrThrow(targetProfileId);

        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new ResourceNotFoundException("You are not following this profile"));

        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public Page<FollowResponse> listFollowers(UUID profileId, Pageable pageable) {
        Profile profile = findProfileByIdOrThrow(profileId);
        return followRepository.findFollowersByProfile(profile, pageable)
                .map(followMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<FollowResponse> listFollowing(UUID profileId, Pageable pageable) {
        Profile profile = findProfileByIdOrThrow(profileId);
        return followRepository.findFollowingByProfile(profile, pageable)
                .map(followMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public FollowStatsResponse getStats(UUID profileId) {
        Profile profile = findProfileByIdOrThrow(profileId);
        long followers = followRepository.countFollowersByProfile(profile);
        long following = followRepository.countFollowingByProfile(profile);
        return new FollowStatsResponse(followers, following);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(User auth, UUID targetProfileId) {
        Profile follower = findProfileByUserOrThrow(auth);
        Profile following = findProfileByIdOrThrow(targetProfileId);
        return followRepository.existsByFollowerAndFollowing(follower, following);
    }

    private Profile findProfileByUserOrThrow(User user) {
        return profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + user.getId()));
    }

    private Profile findProfileByIdOrThrow(UUID id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + id));
    }
}
