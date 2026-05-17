package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.request.profile.UpdateProfileRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileResponse;
import com.github.devlucasjava.socialklyp.application.mapper.ProfileMapper;
import com.github.devlucasjava.socialklyp.application.validator.FileValidator;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.FileReadException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import com.github.devlucasjava.socialklyp.domain.enums.MediaType;
import com.github.devlucasjava.socialklyp.infrastructure.client.port.StoragePort;
import com.github.devlucasjava.socialklyp.infrastructure.client.storage.b2.dto.response.upload.B2UploadFileResponse;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
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
    private final FileValidator fileValidator;
    private final ProfileMapper profileMapper;
    private final StoragePort storagePort;

    @Transactional(readOnly = true)
    public Object findById(UUID id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + id));

        if (profile.isPrivate()) {
            return profileMapper.toSummary(profile);
        }

        return profileMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse findByUser(User auth) {
        Profile profile = findProfileByUserOrThrow(auth);
        return profileMapper.toResponse(profile);
    }

    @Transactional
    public ProfileResponse update(User auth, UpdateProfileRequest request) {
        Profile profile = findProfileByUserOrThrow(auth);

        if (request.bio() != null && !request.bio().isEmpty()) {
            profile.setBio(request.bio());
        }
        if (request.displayName() != null &&
                !request.displayName().isBlank() &&
                request.displayName().trim().length() > 3) {
            profile.setDisplayName(request.displayName());
        }
        if (request.isPrivate() != null) {
            profile.setPrivate(request.isPrivate());
        }
        return profileMapper.toResponse(profileRepository.save(profile));
    }

    @Transactional
    public ProfileResponse updateProfilePicture(User auth, MultipartFile picture) {
        Profile profile = findProfileByUserOrThrow(auth);

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

    private Profile findProfileByUserOrThrow(User auth) {
        return profileRepository.findByUser(auth)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with user: " + auth.getId()));
    }
}