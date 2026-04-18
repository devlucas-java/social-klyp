package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.response.media.MediaResponse;
import com.github.devlucasjava.socialklyp.application.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "List all media of a post")
    public ResponseEntity<List<MediaResponse>> findAllByPost(@PathVariable UUID postId) {
        return ResponseEntity.ok(mediaService.findAllByPost(postId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a media file to a post")
    public ResponseEntity<MediaResponse> upload(
            @PathVariable UUID postId,
            @RequestPart("file") MultipartFile file) {
        MediaResponse response = mediaService.uploadToPost(postId, file);
        URI location = URI.create("/posts/" + postId + "/media/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{mediaId}")
    @Operation(summary = "Delete a media from a post")
    public ResponseEntity<Void> delete(
            //@PathVariable UUID postId,
            @PathVariable UUID mediaId) {
        mediaService.delete(mediaId);
        return ResponseEntity.noContent().build();
    }
}
