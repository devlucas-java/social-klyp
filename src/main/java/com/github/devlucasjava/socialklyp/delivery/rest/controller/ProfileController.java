package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.request.profile.UpdateProfileRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileResponse;
import com.github.devlucasjava.socialklyp.application.dto.response.profile.ProfileSummary;
import com.github.devlucasjava.socialklyp.application.service.ProfileService;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "Get profile by ID",
            description = "Returns full ProfileResponse if public, or ProfileSummary if private"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile found",
                    content = @Content(schema = @Schema(oneOf = {ProfileResponse.class, ProfileSummary.class}))),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content)
    })
    public ResponseEntity<Object> findById(
            @Parameter(description = "Profile UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(profileService.findById(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Returns the full profile of the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<ProfileResponse> findMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.findByUser(user));
    }

    @PutMapping
    @Operation(summary = "Update profile", description = "Updates display name, bio and/or privacy setting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content)
    })
    public ResponseEntity<ProfileResponse> update(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.update(user, request));
    }

    @PatchMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile picture", description = "Uploads or replaces the profile picture (max 5MB, images only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Picture updated",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file (not an image or too large)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content)
    })
    public ResponseEntity<ProfileResponse> updateProfilePicture(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Image file (JPEG, PNG, etc.)", required = true)
            @RequestPart("file") MultipartFile picture) {
        return ResponseEntity.ok(profileService.updateProfilePicture(user, picture));
    }
}
