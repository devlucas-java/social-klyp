package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.request.message.EditMessageRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.message.SendMessageRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.message.MessageResponse;
import com.github.devlucasjava.socialklyp.application.service.MessageService;
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
@RequestMapping("/chats/{chatId}/messages")
@Tag(name = "Messages", description = "Chat message endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    @Operation(summary = "List messages of a chat (members only), ordered by oldest first")
    public ResponseEntity<Page<MessageResponse>> findByChatId(
            @AuthenticationPrincipal User auth,
            @PathVariable UUID chatId,
            Pageable pageable) {
        return ResponseEntity.ok(messageService.findByChatId(auth, chatId, pageable));
    }

    @PostMapping
    @Operation(summary = "Send a message to a chat (members only)")
    public ResponseEntity<MessageResponse> send(
            @AuthenticationPrincipal User auth,
            @PathVariable UUID chatId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messageService.send(auth, chatId, request);
        URI location = URI.create("/chats/" + chatId + "/messages/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{messageId}")
    @Operation(summary = "Edit a message (sender only)")
    public ResponseEntity<MessageResponse> edit(
            @AuthenticationPrincipal User auth,
            @PathVariable UUID chatId,
            @PathVariable UUID messageId,
            @Valid @RequestBody EditMessageRequest request) {
        return ResponseEntity.ok(messageService.edit(auth, messageId, request));
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "Delete a message (sender or chat admin)")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User auth,
            @PathVariable UUID chatId,
            @PathVariable UUID messageId) {
        messageService.delete(auth, messageId);
        return ResponseEntity.noContent().build();
    }
}
