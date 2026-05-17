package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.response.link.LinkResponse;
import com.github.devlucasjava.socialklyp.application.service.LinkService;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/links")
@Tag(name = "Links", description = "Invite link management endpoints for joining chats")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class LinkController {

    private final LinkService linkService;

    @PostMapping("/chats/{chatId}")
    @Operation(
            summary = "Create invite link",
            description = "Generates an invite link for a chat. Admins only. Optionally set an expiration date (ISO-8601)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Link created",
                    content = @Content(schema = @Schema(implementation = LinkResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not an admin", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    public ResponseEntity<LinkResponse> createLink(
            @AuthenticationPrincipal User auth,
            @Parameter(description = "Chat UUID", required = true) @PathVariable UUID chatId,
            @Parameter(description = "Expiration date-time in ISO-8601 format (optional). Example: 2026-12-31T23:59:59")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expirationDate) {
        LinkResponse response = linkService.createLink(auth, chatId, expirationDate);
        URI location = URI.create("/links/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{linkId}/subscribe")
    @Operation(
            summary = "Join via invite link",
            description = "Joins a chat using an invite link. Fails if the link is inactive, expired, or the chat is full (50 members)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Joined the chat successfully"),
            @ApiResponse(responseCode = "403", description = "Link is inactive or expired", content = @Content),
            @ApiResponse(responseCode = "404", description = "Link not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Already a member or chat is full", content = @Content)
    })
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal User auth,
            @Parameter(description = "Link UUID", required = true) @PathVariable UUID linkId) {
        linkService.subscribe(auth, linkId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{linkId}/deactivate")
    @Operation(summary = "Deactivate link", description = "Deactivates an invite link so it can no longer be used. Admins only.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Link deactivated"),
            @ApiResponse(responseCode = "401", description = "Not an admin", content = @Content),
            @ApiResponse(responseCode = "404", description = "Link not found", content = @Content)
    })
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal User auth,
            @Parameter(description = "Link UUID", required = true) @PathVariable UUID linkId) {
        linkService.deactivateLink(auth, linkId);
        return ResponseEntity.noContent().build();
    }
}
