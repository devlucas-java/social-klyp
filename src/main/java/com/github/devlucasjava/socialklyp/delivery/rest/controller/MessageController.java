package com.github.devlucasjava.socialklyp.delivery.rest.controller;

import com.github.devlucasjava.socialklyp.application.dto.request.message.EditMessageRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.message.SendMessageRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.message.MessageResponse;
import com.github.devlucasjava.socialklyp.application.service.MessageService;
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
@RequestMapping("/chats/{chatId}/messages")
@Tag(name = "Messages", description = "Chat message endpoints. Deleted messages keep the record but hide the content.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    @Operation(summary = "List messages", description = "Returns paginated messages of a chat, ordered oldest first. Members only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Messages returned"),
            @ApiResponse(responseCode = "403", description = "Not a member of this chat", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    public ResponseEntity<Page<MessageResponse>> findByChatId(
            @AuthenticationPrincipal User auth,
            @Parameter(description = "Chat UUID", required = true) @PathVariable UUID chatId,
            Pageable pageable) {
        return ResponseEntity.ok(messageService.findByChatId(auth, chatId, pageable));
    }

    @PostMapping
    @Operation(summary = "Send message", description = "Sends a message to a chat. Members only.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Message sent",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "403", description = "Not a member of this chat", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    public ResponseEntity<MessageResponse> send(
            @AuthenticationPrincipal User auth,
            @Parameter(description = "Chat UUID", required = true) @PathVariable UUID chatId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messageService.send(auth, chatId, request);
        URI location = URI.create("/chats/" + chatId + "/messages/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{messageId}")
    @Operation(summary = "Edit message", description = "Edits the content of a message. Sender only. Cannot edit deleted messages.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message edited",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Not the message sender", content = @Content),
            @ApiResponse(responseCode = "403", description = "Message is deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Message not found", content = @Content)
    })
    public ResponseEntity<MessageResponse> edit(
            @AuthenticationPrincipal User auth,
            @Parameter(description = "Chat UUID", required = true) @PathVariable UUID chatId,
            @Parameter(description = "Message UUID", required = true) @PathVariable UUID messageId,
            @Valid @RequestBody EditMessageRequest request) {
        return ResponseEntity.ok(messageService.edit(auth, messageId, request));
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "Delete message", description = "Soft-deletes a message (content is hidden). Sender or chat admin.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Message deleted"),
            @ApiResponse(responseCode = "401", description = "Not the sender or an admin", content = @Content),
            @ApiResponse(responseCode = "404", description = "Message not found", content = @Content)
    })
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User auth,
            @Parameter(description = "Chat UUID", required = true) @PathVariable UUID chatId,
            @Parameter(description = "Message UUID", required = true) @PathVariable UUID messageId) {
        messageService.delete(auth, messageId);
        return ResponseEntity.noContent().build();
    }
}
