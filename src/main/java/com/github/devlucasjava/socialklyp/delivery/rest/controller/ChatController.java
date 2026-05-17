package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.request.chat.CreateChatRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.chat.UpdateChatRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.chat.ChatResponse;
import com.github.devlucasjava.socialklyp.application.service.ChatService;
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
@Tag(name = "Chats", description = "Chat management endpoints. Max 50 members per chat.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    @Operation(summary = "List my chats", description = "Returns all chats the authenticated user belongs to (as member, admin or creator)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chats returned")
    })
    public ResponseEntity<Page<ChatResponse>> listMyChats(
            @AuthenticationPrincipal User auth,
            Pageable pageable) {
        return ResponseEntity.ok(chatService.listMyChats(auth, pageable));
    }

    @GetMapping("/{chatId}")
    @Operation(summary = "Get chat by ID", description = "Returns chat details. Only accessible to members.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chat found",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not a member of this chat", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    public ResponseEntity<ChatResponse> findById(
            @AuthenticationPrincipal User auth,
            @Parameter(description = "Chat UUID", required = true) @PathVariable UUID chatId) {
        return ResponseEntity.ok(chatService.findChatById(auth, chatId));
    }

    @PostMapping
    @Operation(summary = "Create chat", description = "Creates a new chat. The creator is automatically set as admin and member.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chat created",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content)
    })
    public ResponseEntity<ChatResponse> create(
            @AuthenticationPrincipal User auth,
            @Valid @RequestBody CreateChatRequest request) {
        ChatResponse response = chatService.createChat(auth, request);
        URI location = URI.create("/chats/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{chatId}")
    @Operation(summary = "Update chat", description = "Updates the chat name and/or description. Admins only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chat updated",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not an admin", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    public ResponseEntity<ChatResponse> update(
            @AuthenticationPrincipal User auth,
            @Parameter(description = "Chat UUID", required = true) @PathVariable UUID chatId,
            @Valid @RequestBody UpdateChatRequest request) {
        return ResponseEntity.ok(chatService.updateChat(auth, chatId, request));
    }

    @DeleteMapping("/{chatId}")
    @Operation(summary = "Delete chat", description = "Permanently deletes a chat and all its messages. Creator only.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Chat deleted"),
            @ApiResponse(responseCode = "401", description = "Not the chat creator", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User auth,
            @Parameter(description = "Chat UUID", required = true) @PathVariable UUID chatId) {
        chatService.deleteChat(auth, chatId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{chatId}/members/{profileId}")
    @Operation(summary = "Add member", description = "Adds a profile as a member of the chat. Admins only. Max 50 members.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member added",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not an admin", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat or profile not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Already a member or chat is full (50 members)", content = @Content)
    })
    public ResponseEntity<ChatResponse> addMember(
            @AuthenticationPrincipal User auth,
            @Parameter(description = "Chat UUID", required = true) @PathVariable UUID chatId,
            @Parameter(description = "Profile UUID to add", required = true) @PathVariable UUID profileId) {
        return ResponseEntity.ok(chatService.addMember(auth, chatId, profileId));
    }

    @DeleteMapping("/{chatId}/members/{profileId}")
    @Operation(summary = "Remove member", description = "Removes a member from the chat. Admins only. Cannot remove the creator.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member removed",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not an admin", content = @Content),
            @ApiResponse(responseCode = "403", description = "Cannot remove the chat creator", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat or profile not found", content = @Content)
    })
    public ResponseEntity<ChatResponse> removeMember(
            @AuthenticationPrincipal User auth,
            @Parameter(description = "Chat UUID", required = true) @PathVariable UUID chatId,
            @Parameter(description = "Profile UUID to remove", required = true) @PathVariable UUID profileId) {
        return ResponseEntity.ok(chatService.removeMember(auth, chatId, profileId));
    }

    @PostMapping("/{chatId}/admins/{profileId}")
    @Operation(summary = "Promote to admin", description = "Promotes an existing member to admin. Admins only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member promoted",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not an admin", content = @Content),
            @ApiResponse(responseCode = "403", description = "Profile is not a member", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat or profile not found", content = @Content)
    })
    public ResponseEntity<ChatResponse> promoteToAdmin(
            @AuthenticationPrincipal User auth,
            @Parameter(description = "Chat UUID", required = true) @PathVariable UUID chatId,
            @Parameter(description = "Profile UUID to promote", required = true) @PathVariable UUID profileId) {
        return ResponseEntity.ok(chatService.promoteToAdmin(auth, chatId, profileId));
    }
}
