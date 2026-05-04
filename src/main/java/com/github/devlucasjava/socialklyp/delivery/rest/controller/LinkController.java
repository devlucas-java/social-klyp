package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.response.link.LinkResponse;
import com.github.devlucasjava.socialklyp.application.service.LinkService;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Links", description = "Invite link management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class LinkController {

    private final LinkService linkService;

    @PostMapping("/chats/{chatId}")
    @Operation(summary = "Create an invite link for a chat (admins only)")
    public ResponseEntity<LinkResponse> createLink(
            @AuthenticationPrincipal User auth,
            @PathVariable UUID chatId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime expirationDate) {
        LinkResponse response = linkService.createLink(auth, chatId, expirationDate);
        URI location = URI.create("/links/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/{linkId}/subscribe")
    @Operation(summary = "Join a chat using an invite link")
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal User auth,
            @PathVariable UUID linkId) {
        linkService.subscribe(auth, linkId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{linkId}/deactivate")
    @Operation(summary = "Deactivate an invite link (admins only)")
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal User auth,
            @PathVariable UUID linkId) {
        linkService.deactivateLink(auth, linkId);
        return ResponseEntity.noContent().build();
    }
}
