package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.request.chat.CreateChatRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.chat.UpdateChatRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.chat.ChatResponse;
import com.github.devlucasjava.socialklyp.application.service.ChatService;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chats")
@Tag(name = "Chats", description = "Chat management endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    @Operation(summary = "List all chats the authenticated user belongs to")
    public ResponseEntity<Page<ChatResponse>> listMyChats(
            @AuthenticationPrincipal User auth,
            Pageable pageable) {
        return ResponseEntity.ok(chatService.listMyChats(auth, pageable));
    }

    @GetMapping("/{chatId}")
    @Operation(summary = "Get a chat by ID (members only)")
    public ResponseEntity<ChatResponse> findById(
            @AuthenticationPrincipal User auth,
            @PathVariable UUID chatId) {
        return ResponseEntity.ok(chatService.findChatById(auth, chatId));
    }

    @PostMapping
    @Operation(summary = "Create a new chat")
    public ResponseEntity<ChatResponse> create(
            @AuthenticationPrincipal User auth,
            @Valid @RequestBody CreateChatRequest request) {
        ChatResponse response = chatService.createChat(auth, request);
        URI location = URI.create("/chats/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{chatId}")
    @Operation(summary = "Update chat name and description (admins only)")
    public ResponseEntity<ChatResponse> update(
            @AuthenticationPrincipal User auth,
            @PathVariable UUID chatId,
            @Valid @RequestBody UpdateChatRequest request) {
        return ResponseEntity.ok(chatService.updateChat(auth, chatId, request));
    }

    @DeleteMapping("/{chatId}")
    @Operation(summary = "Delete a chat (creator only)")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User auth,
            @PathVariable UUID chatId) {
        chatService.deleteChat(auth, chatId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{chatId}/members/{profileId}")
    @Operation(summary = "Add a member to the chat (admins only)")
    public ResponseEntity<ChatResponse> addMember(
            @AuthenticationPrincipal User auth,
            @PathVariable UUID chatId,
            @PathVariable UUID profileId) {
        return ResponseEntity.ok(chatService.addMember(auth, chatId, profileId));
    }

    @DeleteMapping("/{chatId}/members/{profileId}")
    @Operation(summary = "Remove a member from the chat (admins only)")
    public ResponseEntity<ChatResponse> removeMember(
            @AuthenticationPrincipal User auth,
            @PathVariable UUID chatId,
            @PathVariable UUID profileId) {
        return ResponseEntity.ok(chatService.removeMember(auth, chatId, profileId));
    }

    @PostMapping("/{chatId}/admins/{profileId}")
    @Operation(summary = "Promote a member to admin (admins only)")
    public ResponseEntity<ChatResponse> promoteToAdmin(
            @AuthenticationPrincipal User auth,
            @PathVariable UUID chatId,
            @PathVariable UUID profileId) {
        return ResponseEntity.ok(chatService.promoteToAdmin(auth, chatId, profileId));
    }
}
