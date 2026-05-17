package com.github.devlucasjava.socialklyp.unit.service;

import com.github.devlucasjava.socialklyp.application.dto.request.profile.UpdateProfileRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileResponse;
import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileSummary;
import com.github.devlucasjava.socialklyp.application.mapper.ProfileMapper;
import com.github.devlucasjava.socialklyp.application.service.ProfileService;
import com.github.devlucasjava.socialklyp.application.validator.FileValidator;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.FileReadException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.InvalidFileException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import com.github.devlucasjava.socialklyp.domain.enums.MediaType;
import com.github.devlucasjava.socialklyp.infrastructure.client.port.StoragePort;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2UploadFileResponse;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @InjectMocks
    private ProfileService profileService;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private FileValidator fileValidator;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private StoragePort storagePort;

    @Mock
    private MultipartFile multipartFile;

    private User authUser;
    private Profile publicProfile;
    private Profile privateProfile;
    private ProfileResponse profileResponse;
    private ProfileSummary profileSummary;

    private final UUID profileId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        authUser = new User();
        authUser.setId(userId);

        publicProfile = new Profile();
        publicProfile.setId(profileId);
        publicProfile.setDisplayName("Public User");
        publicProfile.setBio("My bio");
        publicProfile.setPrivate(false);
        publicProfile.setUser(authUser);

        privateProfile = new Profile();
        privateProfile.setId(UUID.randomUUID());
        privateProfile.setDisplayName("Private User");
        privateProfile.setBio("Hidden bio");
        privateProfile.setPrivate(true);
        privateProfile.setUser(authUser);

        profileResponse = new ProfileResponse(profileId, "Public User", "My bio", null, false, userId);
        profileSummary = new ProfileSummary(privateProfile.getId(), "Private User", null, true);
    }

    // -------------------- FIND BY ID --------------------

    @Test
    void shouldReturnFullResponseWhenProfileIsPublic() {
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(publicProfile));
        when(profileMapper.toResponse(publicProfile)).thenReturn(profileResponse);

        Object result = profileService.findById(profileId);

        assertInstanceOf(ProfileResponse.class, result);
        assertEquals(profileId, ((ProfileResponse) result).id());
        verify(profileMapper).toResponse(publicProfile);
        verify(profileMapper, never()).toSummary(any());
    }

    @Test
    void shouldReturnSummaryWhenProfileIsPrivate() {
        when(profileRepository.findById(privateProfile.getId())).thenReturn(Optional.of(privateProfile));
        when(profileMapper.toSummary(privateProfile)).thenReturn(profileSummary);

        Object result = profileService.findById(privateProfile.getId());

        assertInstanceOf(ProfileSummary.class, result);
        assertTrue(((ProfileSummary) result).isPrivate());
        verify(profileMapper).toSummary(privateProfile);
        verify(profileMapper, never()).toResponse(any());
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnFindById() {
        UUID unknownId = UUID.randomUUID();
        when(profileRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> profileService.findById(unknownId));
    }

    // -------------------- FIND BY USER --------------------

    @Test
    void shouldFindProfileByUser() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));
        when(profileMapper.toResponse(publicProfile)).thenReturn(profileResponse);

        ProfileResponse result = profileService.findByUser(authUser);

        assertNotNull(result);
        assertEquals(profileId, result.id());
    }

    @Test
    void shouldThrowWhenProfileNotFoundByUser() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> profileService.findByUser(authUser));
    }

    // -------------------- UPDATE --------------------

    @Test
    void shouldUpdateBioSuccessfully() {
        UpdateProfileRequest request = new UpdateProfileRequest(null, "New bio", null);
        ProfileResponse updatedResponse = new ProfileResponse(profileId, "Public User", "New bio", null, false, userId);

        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));
        when(profileRepository.save(publicProfile)).thenReturn(publicProfile);
        when(profileMapper.toResponse(publicProfile)).thenReturn(updatedResponse);

        ProfileResponse result = profileService.update(authUser, request);

        assertEquals("New bio", result.bio());
        verify(profileRepository).save(publicProfile);
    }

    @Test
    void shouldUpdateDisplayNameSuccessfully() {
        UpdateProfileRequest request = new UpdateProfileRequest("Jane Doe", null, null);
        ProfileResponse updatedResponse = new ProfileResponse(profileId, "Jane Doe", "My bio", null, false, userId);

        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));
        when(profileRepository.save(publicProfile)).thenReturn(publicProfile);
        when(profileMapper.toResponse(publicProfile)).thenReturn(updatedResponse);

        ProfileResponse result = profileService.update(authUser, request);

        assertEquals("Jane Doe", result.displayName());
    }

    @Test
    void shouldNotUpdateDisplayNameWhenTooShort() {
        UpdateProfileRequest request = new UpdateProfileRequest("Jo", null, null);

        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));
        when(profileRepository.save(publicProfile)).thenReturn(publicProfile);
        when(profileMapper.toResponse(publicProfile)).thenReturn(profileResponse);

        profileService.update(authUser, request);

        // displayName should remain unchanged — "Jo" has length <= 3
        assertEquals("Public User", publicProfile.getDisplayName());
    }

    @Test
    void shouldUpdateIsPrivateToTrue() {
        UpdateProfileRequest request = new UpdateProfileRequest(null, null, true);
        ProfileResponse updatedResponse = new ProfileResponse(profileId, "Public User", "My bio", null, true, userId);

        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));
        when(profileRepository.save(publicProfile)).thenReturn(publicProfile);
        when(profileMapper.toResponse(publicProfile)).thenReturn(updatedResponse);

        ProfileResponse result = profileService.update(authUser, request);

        assertTrue(result.isPrivate());
    }

    @Test
    void shouldNotChangeBioWhenBioIsNull() {
        UpdateProfileRequest request = new UpdateProfileRequest(null, null, null);

        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));
        when(profileRepository.save(publicProfile)).thenReturn(publicProfile);
        when(profileMapper.toResponse(publicProfile)).thenReturn(profileResponse);

        profileService.update(authUser, request);

        assertEquals("My bio", publicProfile.getBio());
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnUpdate() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> profileService.update(authUser, new UpdateProfileRequest("Name", "bio", false)));
    }

    // -------------------- UPDATE PROFILE PICTURE --------------------

    @Test
    void shouldUpdateProfilePictureSuccessfully() throws IOException {
        B2UploadFileResponse b2Response = new B2UploadFileResponse(
                "b2-file-id", "avatar.jpg", "https://cdn.example.com/avatar.jpg",
                "account-id", "bucket-id", 512L, "sha1", "image/jpeg"
        );
        ProfileResponse updatedResponse = new ProfileResponse(profileId, "Public User", "My bio",
                "https://cdn.example.com/avatar.jpg", false, userId);

        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(multipartFile.getSize()).thenReturn(512L);
        when(multipartFile.getOriginalFilename()).thenReturn("avatar.jpg");
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(storagePort.upload(any(InputStream.class), anyLong(), anyString(), anyString(), anyBoolean(), eq(MediaType.IMAGE)))
                .thenReturn(b2Response);
        when(profileRepository.save(publicProfile)).thenReturn(publicProfile);
        when(profileMapper.toResponse(publicProfile)).thenReturn(updatedResponse);

        ProfileResponse result = profileService.updateProfilePicture(authUser, multipartFile);

        assertNotNull(result);
        assertEquals("https://cdn.example.com/avatar.jpg", result.profilePictureUrl());
        verify(profileRepository).save(publicProfile);
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnUpdatePicture() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> profileService.updateProfilePicture(authUser, multipartFile));
    }

    @Test
    void shouldThrowWhenFileIsInvalidOnUpdatePicture() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));
        doThrow(new InvalidFileException("Only images allowed"))
                .when(fileValidator).validateImageOnly(multipartFile);

        assertThrows(InvalidFileException.class,
                () -> profileService.updateProfilePicture(authUser, multipartFile));

        verify(storagePort, never()).upload(any(), anyLong(), anyString(), anyString(), anyBoolean(), any());
    }

    @Test
    void shouldThrowFileReadExceptionWhenIOExceptionOccurs() throws IOException {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(publicProfile));
        when(multipartFile.getInputStream()).thenThrow(new IOException("disk error"));

        assertThrows(FileReadException.class,
                () -> profileService.updateProfilePicture(authUser, multipartFile));

        verify(profileRepository, never()).save(any());
    }
}
