package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.response.media.MediaResponse;
import com.github.devlucasjava.socialklyp.application.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/media")
@Tag(name = "Media", description = "Media upload and management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class MediaController {

    private final MediaService mediaService;

    @GetMapping
    @Operation(summary = "List media", description = "Returns all media files attached to a post")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Media list returned"),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
    })
    public ResponseEntity<List<MediaResponse>> findAllByPost(
            @Parameter(description = "Post UUID", required = true) @PathVariable UUID postId) {
        return ResponseEntity.ok(mediaService.findAllByPost(postId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload media", description = "Uploads an image to a post (max 5MB, images only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Media uploaded",
                    content = @Content(schema = @Schema(implementation = MediaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file (not an image or exceeds 5MB)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content)
    })
    public ResponseEntity<MediaResponse> upload(
            @Parameter(description = "Post UUID", required = true) @PathVariable UUID postId,
            @Parameter(description = "Image file", required = true)
            @RequestPart("file") MultipartFile file) {
        MediaResponse response = mediaService.uploadToPost(postId, file);
        URI location = URI.create("/posts/" + postId + "/media/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{mediaId}")
    @Operation(summary = "Delete media", description = "Deletes a media file from storage and database")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Media deleted"),
            @ApiResponse(responseCode = "404", description = "Media not found", content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Media UUID", required = true) @PathVariable UUID mediaId) {
        mediaService.delete(mediaId);
        return ResponseEntity.noContent().build();
    }
}
