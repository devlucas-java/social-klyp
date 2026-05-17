package com.github.devlucasjava.socialklyp.unit.service;

import com.github.devlucasjava.socialklyp.application.dto.response.like.LikeResponse;
import com.github.devlucasjava.socialklyp.application.dto.response.utils.BooleanDTO;
import com.github.devlucasjava.socialklyp.application.mapper.LikeMapper;
import com.github.devlucasjava.socialklyp.application.service.LikeService;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ConflictException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Like;
import com.github.devlucasjava.socialklyp.domain.entity.Post;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.LikeRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.PostRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private LikeMapper likeMapper;

    private User authUser;
    private Profile profile;
    private Post post;
    private Like like;

    private final UUID postId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();
    private final UUID likeId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        authUser = new User();
        authUser.setId(UUID.randomUUID());

        profile = new Profile();
        profile.setId(profileId);
        profile.setDisplayName("Test User");

        post = new Post();
        post.setId(postId);
        post.setContent("Post content");
        post.setProfile(profile);

        like = new Like();
        like.setId(likeId);
        like.setProfile(profile);
        like.setPost(post);
    }

    // -------------------- HAS LIKED --------------------

    @Test
    void shouldReturnTrueWhenUserAlreadyLikedPost() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(profile));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likeRepository.existsByProfileIdAndPostId(profileId, postId)).thenReturn(true);

        BooleanDTO result = likeService.hasLikePost(postId, authUser);

        assertTrue(result.isValid());
    }

    @Test
    void shouldReturnFalseWhenUserHasNotLikedPost() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(profile));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likeRepository.existsByProfileIdAndPostId(profileId, postId)).thenReturn(false);

        BooleanDTO result = likeService.hasLikePost(postId, authUser);

        assertFalse(result.isValid());
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnHasLike() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> likeService.hasLikePost(postId, authUser));
    }

    @Test
    void shouldThrowWhenPostNotFoundOnHasLike() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(profile));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> likeService.hasLikePost(postId, authUser));
    }

    // -------------------- LIKE POST --------------------

    @Test
    void shouldLikePostSuccessfully() {
        LikeResponse likeResponse = new LikeResponse(likeId, profileId, postId);

        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(profile));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likeRepository.existsByProfileIdAndPostId(profileId, postId)).thenReturn(false);
        when(likeRepository.save(any(Like.class))).thenReturn(like);
        when(likeMapper.toResponse(like)).thenReturn(likeResponse);

        LikeResponse result = likeService.likePost(postId, authUser);

        assertNotNull(result);
        assertEquals(likeId, result.id());
        assertEquals(profileId, result.profileId());
        assertEquals(postId, result.postId());
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    void shouldThrowWhenAlreadyLiked() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(profile));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likeRepository.existsByProfileIdAndPostId(profileId, postId)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> likeService.likePost(postId, authUser));

        verify(likeRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnLike() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> likeService.likePost(postId, authUser));
    }

    @Test
    void shouldThrowWhenPostNotFoundOnLike() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(profile));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> likeService.likePost(postId, authUser));
    }

    // -------------------- UNLIKE POST --------------------

    @Test
    void shouldUnlikePostSuccessfully() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(profile));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likeRepository.findByProfileIdAndPostId(profileId, postId)).thenReturn(Optional.of(like));

        likeService.unlikePost(postId, authUser);

        verify(likeRepository).delete(like);
    }

    @Test
    void shouldThrowWhenLikeNotFoundOnUnlike() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(profile));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likeRepository.findByProfileIdAndPostId(profileId, postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> likeService.unlikePost(postId, authUser));

        verify(likeRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnUnlike() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> likeService.unlikePost(postId, authUser));
    }

    @Test
    void shouldThrowWhenPostNotFoundOnUnlike() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(profile));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> likeService.unlikePost(postId, authUser));
    }

    // -------------------- COUNT LIKES --------------------

    @Test
    void shouldCountLikesByPost() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likeRepository.countByPostId(postId)).thenReturn(5L);

        long count = likeService.countLikesByPost(postId);

        assertEquals(5L, count);
    }

    @Test
    void shouldReturnZeroWhenNoLikes() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likeRepository.countByPostId(postId)).thenReturn(0L);

        long count = likeService.countLikesByPost(postId);

        assertEquals(0L, count);
    }

    @Test
    void shouldThrowWhenPostNotFoundOnCount() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> likeService.countLikesByPost(postId));
    }
}
