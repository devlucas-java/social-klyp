package com.github.devlucasjava.socialklyp.unit.service;

import com.github.devlucasjava.socialklyp.application.dto.request.post.CreatePostRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.post.UpdatePostRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.post.PostResponse;
import com.github.devlucasjava.socialklyp.application.mapper.PostMapper;
import com.github.devlucasjava.socialklyp.application.service.PostService;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ForbiddenException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.FollowRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.PostRepository;
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
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private PostMapper postMapper;

    private User authUser;
    private Profile publicProfile;
    private Profile privateProfile;
    private Post post;
    private PostResponse postResponse;
    private final UUID postId = UUID.randomUUID();
    private final UUID publicProfileId = UUID.randomUUID();
    private final UUID privateProfileId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        authUser = new User();
        authUser.setId(UUID.randomUUID());

        publicProfile = new Profile();
        publicProfile.setId(publicProfileId);
        publicProfile.setDisplayName("Public User");
        publicProfile.setPrivate(false);

        privateProfile = new Profile();
        privateProfile.setId(privateProfileId);
        privateProfile.setDisplayName("Private User");
        privateProfile.setPrivate(true);

        post = new Post();
        post.setId(postId);
        post.setContent("Test content");
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setProfile(publicProfile);

        postResponse = new PostResponse(postId, "Test content", null, post.getCreatedAt(), post.getUpdatedAt());
    }

    // -------------------- FIND ALL --------------------

    @Test
    void shouldFindAllPublicPostsOnly() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> page = new PageImpl<>(List.of(post));

        when(postRepository.findByProfileIsPrivateFalse(pageable)).thenReturn(page);
        when(postMapper.toResponse(post)).thenReturn(postResponse);

        Page<PostResponse> result = postService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        verify(postRepository).findByProfileIsPrivateFalse(pageable);
        verify(postRepository, never()).findAll(pageable);
    }

    // -------------------- FIND MY --------------------

    @Test
    void shouldFindMyPostsSuccessfully() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> page = new PageImpl<>(List.of(post));

        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));
        when(postRepository.findByProfile(publicProfile, pageable)).thenReturn(page);
        when(postMapper.toResponse(post)).thenReturn(postResponse);

        Page<PostResponse> result = postService.findMy(authUser, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnFindMy() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> postService.findMy(authUser, PageRequest.of(0, 10)));
    }

    // -------------------- FIND BY ID --------------------

    @Test
    void shouldFindByIdWhenProfileIsPublic() {
        post.setProfile(publicProfile);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMapper.toResponse(post)).thenReturn(postResponse);

        PostResponse result = postService.findById(authUser, postId);

        assertNotNull(result);
        assertEquals(postId, result.id());
        verify(profileRepository, never()).findByUser(any());
    }

    @Test
    void shouldFindByIdWhenProfileIsPrivateAndRequesterIsOwner() {
        post.setProfile(privateProfile);

        Profile requesterProfile = new Profile();
        requesterProfile.setId(privateProfileId); // mesmo ID = dono

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(requesterProfile));
        when(postMapper.toResponse(post)).thenReturn(postResponse);

        PostResponse result = postService.findById(authUser, postId);

        assertNotNull(result);
        verify(followRepository, never()).existsByFollowerAndFollowing(any(), any());
    }

    @Test
    void shouldFindByIdWhenProfileIsPrivateAndRequesterFollows() {
        post.setProfile(privateProfile);

        Profile requesterProfile = new Profile();
        requesterProfile.setId(UUID.randomUUID()); // ID diferente = não é dono

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(requesterProfile));
        when(followRepository.existsByFollowerAndFollowing(requesterProfile, privateProfile)).thenReturn(true);
        when(postMapper.toResponse(post)).thenReturn(postResponse);

        PostResponse result = postService.findById(authUser, postId);

        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenProfileIsPrivateAndRequesterDoesNotFollow() {
        post.setProfile(privateProfile);

        Profile requesterProfile = new Profile();
        requesterProfile.setId(UUID.randomUUID()); // ID diferente = não é dono

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(requesterProfile));
        when(followRepository.existsByFollowerAndFollowing(requesterProfile, privateProfile)).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> postService.findById(authUser, postId));
    }

    @Test
    void shouldThrowWhenPostNotFoundOnFindById() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> postService.findById(authUser, postId));
    }

    // -------------------- FIND BY PROFILE --------------------

    @Test
    void shouldFindByPublicProfileSuccessfully() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> page = new PageImpl<>(List.of(post));

        when(profileRepository.findById(publicProfileId)).thenReturn(Optional.of(publicProfile));
        when(postRepository.findByProfile(publicProfile, pageable)).thenReturn(page);
        when(postMapper.toResponse(post)).thenReturn(postResponse);

        Page<PostResponse> result = postService.findByProfile(authUser, publicProfileId, pageable);

        assertEquals(1, result.getTotalElements());
        verify(profileRepository, never()).findByUser(any());
    }

    @Test
    void shouldFindByPrivateProfileWhenRequesterIsOwner() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> page = new PageImpl<>(List.of(post));

        Profile ownerProfile = new Profile();
        ownerProfile.setId(privateProfileId); // mesmo ID = dono

        when(profileRepository.findById(privateProfileId)).thenReturn(Optional.of(privateProfile));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(ownerProfile));
        when(postRepository.findByProfile(privateProfile, pageable)).thenReturn(page);
        when(postMapper.toResponse(post)).thenReturn(postResponse);

        Page<PostResponse> result = postService.findByProfile(authUser, privateProfileId, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldFindByPrivateProfileWhenRequesterFollows() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> page = new PageImpl<>(List.of(post));

        Profile requesterProfile = new Profile();
        requesterProfile.setId(UUID.randomUUID());

        when(profileRepository.findById(privateProfileId)).thenReturn(Optional.of(privateProfile));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(requesterProfile));
        when(followRepository.existsByFollowerAndFollowing(requesterProfile, privateProfile)).thenReturn(true);
        when(postRepository.findByProfile(privateProfile, pageable)).thenReturn(page);
        when(postMapper.toResponse(post)).thenReturn(postResponse);

        Page<PostResponse> result = postService.findByProfile(authUser, privateProfileId, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldThrowWhenPrivateProfileAndRequesterDoesNotFollow() {
        Pageable pageable = PageRequest.of(0, 10);

        Profile requesterProfile = new Profile();
        requesterProfile.setId(UUID.randomUUID());

        when(profileRepository.findById(privateProfileId)).thenReturn(Optional.of(privateProfile));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(requesterProfile));
        when(followRepository.existsByFollowerAndFollowing(requesterProfile, privateProfile)).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> postService.findByProfile(authUser, privateProfileId, pageable));
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnFindByProfile() {
        when(profileRepository.findById(publicProfileId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> postService.findByProfile(authUser, publicProfileId, PageRequest.of(0, 10)));
    }

    // -------------------- CREATE --------------------

    @Test
    void shouldCreatePostSuccessfully() {
        CreatePostRequest request = new CreatePostRequest("New post content");

        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));
        when(postMapper.toEntity(request)).thenReturn(post);
        when(postRepository.save(post)).thenReturn(post);
        when(postMapper.toResponse(post)).thenReturn(postResponse);

        PostResponse result = postService.create(authUser, request);

        assertNotNull(result);
        verify(postRepository).save(post);
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnCreate() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> postService.create(authUser, new CreatePostRequest("content")));
    }

    // -------------------- UPDATE --------------------

    @Test
    void shouldUpdatePostSuccessfully() {
        UpdatePostRequest request = new UpdatePostRequest("Updated content");
        post.setProfile(publicProfile);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));
        when(postRepository.save(post)).thenReturn(post);
        when(postMapper.toResponse(post)).thenReturn(postResponse);

        PostResponse result = postService.update(postId, authUser, request);

        assertNotNull(result);
        verify(postRepository).save(post);
    }

    @Test
    void shouldThrowWhenUpdatingPostOfAnotherProfile() {
        Profile otherProfile = new Profile();
        otherProfile.setId(UUID.randomUUID());
        post.setProfile(otherProfile);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));

        assertThrows(ForbiddenException.class,
                () -> postService.update(postId, authUser, new UpdatePostRequest("content")));
    }

    // -------------------- DELETE --------------------

    @Test
    void shouldDeletePostSuccessfully() {
        post.setProfile(publicProfile);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));

        postService.delete(authUser, postId);

        verify(postRepository).deleteById(postId);
    }

    @Test
    void shouldThrowWhenDeletingPostOfAnotherProfile() {
        Profile otherProfile = new Profile();
        otherProfile.setId(UUID.randomUUID());
        post.setProfile(otherProfile);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));

        assertThrows(ForbiddenException.class,
                () -> postService.delete(authUser, postId));

        verify(postRepository, never()).deleteById(any());
    }
}
