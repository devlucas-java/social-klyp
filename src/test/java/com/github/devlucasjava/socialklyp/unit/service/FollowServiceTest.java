package com.github.devlucasjava.socialklyp.unit.service;

import com.github.devlucasjava.socialklyp.application.dto.response.follow.FollowResponse;
import com.github.devlucasjava.socialklyp.application.dto.response.follow.FollowStatsResponse;
import com.github.devlucasjava.socialklyp.application.mapper.FollowMapper;
import com.github.devlucasjava.socialklyp.application.service.FollowService;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ConflictException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ForbiddenException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Follow;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.FollowRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @InjectMocks
    private FollowService followService;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private FollowMapper followMapper;

    private User authUser;
    private Profile followerProfile;
    private Profile followingProfile;
    private final UUID followerProfileId = UUID.randomUUID();
    private final UUID followingProfileId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        authUser = new User();
        authUser.setId(UUID.randomUUID());

        followerProfile = new Profile();
        followerProfile.setId(followerProfileId);
        followerProfile.setDisplayName("Follower User");

        followingProfile = new Profile();
        followingProfile.setId(followingProfileId);
        followingProfile.setDisplayName("Following User");
    }

    // -------------------- FOLLOW --------------------

    @Test
    void shouldFollowSuccessfully() {
        Follow follow = new Follow();
        follow.setId(UUID.randomUUID());
        follow.setFollower(followerProfile);
        follow.setFollowing(followingProfile);
        follow.setCreatedAt(LocalDateTime.now());

        FollowResponse expectedResponse = new FollowResponse(
                follow.getId(),
                followerProfileId,
                "Follower User",
                null,
                followingProfileId,
                "Following User",
                null,
                follow.getCreatedAt()
        );

        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(followerProfile));
        when(profileRepository.findById(followingProfileId)).thenReturn(Optional.of(followingProfile));
        when(followRepository.existsByFollowerAndFollowing(followerProfile, followingProfile)).thenReturn(false);
        when(followRepository.save(any(Follow.class))).thenReturn(follow);
        when(followMapper.toResponse(follow)).thenReturn(expectedResponse);

        FollowResponse result = followService.follow(authUser, followingProfileId);

        assertNotNull(result);
        assertEquals(followerProfileId, result.followerId());
        assertEquals(followingProfileId, result.followingId());
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void shouldThrowWhenFollowingSelf() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(followerProfile));
        when(profileRepository.findById(followerProfileId)).thenReturn(Optional.of(followerProfile));

        assertThrows(ForbiddenException.class,
                () -> followService.follow(authUser, followerProfileId));

        verify(followRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAlreadyFollowing() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(followerProfile));
        when(profileRepository.findById(followingProfileId)).thenReturn(Optional.of(followingProfile));
        when(followRepository.existsByFollowerAndFollowing(followerProfile, followingProfile)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> followService.follow(authUser, followingProfileId));

        verify(followRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenFollowerProfileNotFound() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> followService.follow(authUser, followingProfileId));
    }

    @Test
    void shouldThrowWhenTargetProfileNotFoundOnFollow() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(followerProfile));
        when(profileRepository.findById(followingProfileId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> followService.follow(authUser, followingProfileId));
    }

    // -------------------- UNFOLLOW --------------------

    @Test
    void shouldUnfollowSuccessfully() {
        Follow follow = new Follow();
        follow.setId(UUID.randomUUID());
        follow.setFollower(followerProfile);
        follow.setFollowing(followingProfile);

        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(followerProfile));
        when(profileRepository.findById(followingProfileId)).thenReturn(Optional.of(followingProfile));
        when(followRepository.findByFollowerAndFollowing(followerProfile, followingProfile))
                .thenReturn(Optional.of(follow));

        followService.unfollow(authUser, followingProfileId);

        verify(followRepository).delete(follow);
    }

    @Test
    void shouldThrowWhenNotFollowingOnUnfollow() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(followerProfile));
        when(profileRepository.findById(followingProfileId)).thenReturn(Optional.of(followingProfile));
        when(followRepository.findByFollowerAndFollowing(followerProfile, followingProfile))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> followService.unfollow(authUser, followingProfileId));

        verify(followRepository, never()).delete(any());
    }

    // -------------------- LIST FOLLOWERS --------------------

    @Test
    void shouldListFollowersSuccessfully() {
        Follow follow = new Follow();
        follow.setId(UUID.randomUUID());
        follow.setFollower(followerProfile);
        follow.setFollowing(followingProfile);
        follow.setCreatedAt(LocalDateTime.now());

        FollowResponse followResponse = new FollowResponse(
                follow.getId(), followerProfileId, "Follower User", null,
                followingProfileId, "Following User", null, follow.getCreatedAt()
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<Follow> followPage = new PageImpl<>(List.of(follow));

        when(profileRepository.findById(followingProfileId)).thenReturn(Optional.of(followingProfile));
        when(followRepository.findFollowersByProfile(followingProfile, pageable)).thenReturn(followPage);
        when(followMapper.toResponse(follow)).thenReturn(followResponse);

        Page<FollowResponse> result = followService.listFollowers(followingProfileId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(followerProfileId, result.getContent().get(0).followerId());
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnListFollowers() {
        when(profileRepository.findById(followingProfileId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> followService.listFollowers(followingProfileId, PageRequest.of(0, 10)));
    }

    // -------------------- LIST FOLLOWING --------------------

    @Test
    void shouldListFollowingSuccessfully() {
        Follow follow = new Follow();
        follow.setId(UUID.randomUUID());
        follow.setFollower(followerProfile);
        follow.setFollowing(followingProfile);
        follow.setCreatedAt(LocalDateTime.now());

        FollowResponse followResponse = new FollowResponse(
                follow.getId(), followerProfileId, "Follower User", null,
                followingProfileId, "Following User", null, follow.getCreatedAt()
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<Follow> followPage = new PageImpl<>(List.of(follow));

        when(profileRepository.findById(followerProfileId)).thenReturn(Optional.of(followerProfile));
        when(followRepository.findFollowingByProfile(followerProfile, pageable)).thenReturn(followPage);
        when(followMapper.toResponse(follow)).thenReturn(followResponse);

        Page<FollowResponse> result = followService.listFollowing(followerProfileId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(followingProfileId, result.getContent().get(0).followingId());
    }

    // -------------------- STATS --------------------

    @Test
    void shouldReturnFollowStats() {
        when(profileRepository.findById(followingProfileId)).thenReturn(Optional.of(followingProfile));
        when(followRepository.countFollowersByProfile(followingProfile)).thenReturn(5L);
        when(followRepository.countFollowingByProfile(followingProfile)).thenReturn(3L);

        FollowStatsResponse stats = followService.getStats(followingProfileId);

        assertEquals(5L, stats.followers());
        assertEquals(3L, stats.following());
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnGetStats() {
        when(profileRepository.findById(followingProfileId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> followService.getStats(followingProfileId));
    }

    // -------------------- IS FOLLOWING --------------------

    @Test
    void shouldReturnTrueWhenFollowing() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(followerProfile));
        when(profileRepository.findById(followingProfileId)).thenReturn(Optional.of(followingProfile));
        when(followRepository.existsByFollowerAndFollowing(followerProfile, followingProfile)).thenReturn(true);

        assertTrue(followService.isFollowing(authUser, followingProfileId));
    }

    @Test
    void shouldReturnFalseWhenNotFollowing() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(followerProfile));
        when(profileRepository.findById(followingProfileId)).thenReturn(Optional.of(followingProfile));
        when(followRepository.existsByFollowerAndFollowing(followerProfile, followingProfile)).thenReturn(false);

        assertFalse(followService.isFollowing(authUser, followingProfileId));
    }
}
