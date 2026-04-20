package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.request.profile.UpdateProfileRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileResponse;
import com.github.devlucasjava.socialklyp.application.service.ProfileService;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profiles")
@Tag(name = "Profiles", description = "Profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{id}")
    @Operation(summary = "Get a profile by ID")
    public ResponseEntity<Object> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(profileService.findById(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    public ResponseEntity<ProfileResponse> findMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.findByUser(user));
    }

    @PutMapping
    @Operation(summary = "Update a profile")
    public ResponseEntity<ProfileResponse> update(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.update(user, request));
    }

    @PatchMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload or update profile picture")
    public ResponseEntity<ProfileResponse> updateProfilePicture(
            @AuthenticationPrincipal User user,
            @RequestPart("file") MultipartFile picture) {
        return ResponseEntity.ok(profileService.updateProfilePicture(user, picture));
    }
}
