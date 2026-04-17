package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.request.profile.CreateProfileRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.profile.UpdateProfileRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileResponse;
import com.github.devlucasjava.socialklyp.application.mapper.ProfileMapper;
import com.github.devlucasjava.socialklyp.application.validator.FileValidator;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.FileReadException;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import com.github.devlucasjava.socialklyp.domain.enuns.MediaType;
import com.github.devlucasjava.socialklyp.infrastructure.client.port.StoragePort;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2UploadFileResponse;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final FileValidator fileValidator;
    private final ProfileMapper profileMapper;
    private final StoragePort storagePort;

    @Transactional(readOnly = true)
    public ProfileResponse findById(UUID id) {
        return profileMapper.toResponse(findProfileOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ProfileResponse findByUserId(UUID userId) {
        return profileRepository.findByUserId(userId)
                .map(profileMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found for user id: " + userId));
    }

    @Transactional
    public ProfileResponse create(UUID userId, CreateProfileRequest request) {
        User user = findUserOrThrow(userId);

        if (profileRepository.existsByUserId(userId)) {
            throw new IllegalStateException("User already has a profile");
        }

        Profile profile = profileMapper.toEntity(request);
        profile.setUser(user);

        return profileMapper.toResponse(profileRepository.save(profile));
    }

    @Transactional
    public ProfileResponse update(UUID id, UpdateProfileRequest request) {
        Profile profile = findProfileOrThrow(id);
        profile.setDisplayName(request.displayName());
        profile.setBio(request.bio());
        profile.setPrivate(request.isPrivate());
        return profileMapper.toResponse(profileRepository.save(profile));
    }

    @Transactional
    public ProfileResponse updateProfilePicture(UUID id, MultipartFile picture) {
        Profile profile = findProfileOrThrow(id);

        fileValidator.validateImageOnly(picture);

        try {
            B2UploadFileResponse result = null;
            result = storagePort.upload(
                    picture.getInputStream(),
                    picture.getSize(),
                    picture.getOriginalFilename(),
                    picture.getContentType(),
                    false,
                    MediaType.IMAGE
            );
            profile.setProfilePictureUrl(result.fileUrl());
        } catch (IOException e) {
            throw new FileReadException("Error reading file");
        }

        return profileMapper.toResponse(profileRepository.save(profile));
    }

    @Transactional
    public void delete(UUID id) {
        findProfileOrThrow(id);
        profileRepository.deleteById(id);
    }

    private Profile findProfileOrThrow(UUID id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found with id: " + id));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }
}
