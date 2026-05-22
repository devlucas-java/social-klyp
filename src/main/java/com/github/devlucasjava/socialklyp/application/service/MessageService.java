package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.request.message.EditMessageRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.message.SendMessageRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.message.MessageResponse;
import com.github.devlucasjava.socialklyp.application.dto.websocket.WsEventType;
import com.github.devlucasjava.socialklyp.application.dto.websocket.WsMessageEvent;
import com.github.devlucasjava.socialklyp.application.mapper.MessageMapper;
import com.github.devlucasjava.socialklyp.application.mapper.ProfileMapper;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ForbiddenException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.UnauthorizeException;
import com.github.devlucasjava.socialklyp.domain.entity.Chat;
import com.github.devlucasjava.socialklyp.domain.entity.Message;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ChatRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.MessageRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    /** Messages can only be edited within this window after creation. */
    private static final int EDIT_WINDOW_MINUTES = 30;

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final ProfileMapper profileMapper;

    // ================================================================
    // REST — used by MessageController
    // ================================================================

    @Transactional(readOnly = true)
    public Page<MessageResponse> findByChatId(User auth, UUID chatId, Pageable pageable) {
        Chat chat = getChatOrThrow(chatId);
        Profile profile = getProfileOrThrow(auth);
        validateMember(profile, chat);
        return messageRepository.findByChatId(chatId, pageable).map(messageMapper::toResponse);
    }

    @Transactional
    public MessageResponse send(User auth, UUID chatId, SendMessageRequest request) {
        Chat chat = getChatOrThrow(chatId);
        Profile profile = getProfileOrThrow(auth);
        validateMember(profile, chat);
        return messageMapper.toResponse(messageRepository.save(buildMessage(chat, profile, request.content())));
    }

    @Transactional
    public MessageResponse edit(User auth, UUID messageId, EditMessageRequest request) {
        Message message = getMessageOrThrow(messageId);
        Profile profile = getProfileOrThrow(auth);
        validateSender(profile, message);
        validateNotDeleted(message);
        validateEditWindow(message);
        message.setContent(request.content());
        return messageMapper.toResponse(messageRepository.save(message));
    }

    @Transactional
    public void delete(User auth, UUID messageId) {
        Message message = getMessageOrThrow(messageId);
        Profile profile = getProfileOrThrow(auth);
        validateCanDelete(profile, message);
        softDelete(message);
        messageRepository.save(message);
    }

    // ================================================================
    // WebSocket / STOMP — used by StompChatController
    // Receives username (String) from the authenticated Principal.
    // Returns WsMessageEvent ready for broadcast via SimpMessagingTemplate.
    // ================================================================

    /**
     * Persists a new message and returns the broadcast event.
     */
    @Transactional
    public WsMessageEvent sendViaWebSocket(String username, UUID chatId, String content) {
        User user = getUserByUsernameOrThrow(username);
        Chat chat = getChatOrThrow(chatId);
        Profile profile = getProfileOrThrow(user);
        validateMember(profile, chat);

        Message saved = messageRepository.save(buildMessage(chat, profile, content));

        return WsMessageEvent.builder()
                .type(WsEventType.MESSAGE)
                .messageId(saved.getId())
                .chatId(chatId)
                .content(saved.getContent())
                .sender(profileMapper.toSummary(profile))
                .deleted(false)
                .timestamp(saved.getCreatedAt())
                .build();
    }

    /**
     * Edits a message and returns the broadcast event.
     * Only editable within {@value EDIT_WINDOW_MINUTES} minutes.
     */
    @Transactional
    public WsMessageEvent editViaWebSocket(String username, UUID messageId, String newContent) {
        User user = getUserByUsernameOrThrow(username);
        Message message = getMessageOrThrow(messageId);
        Profile profile = getProfileOrThrow(user);
        validateSender(profile, message);
        validateNotDeleted(message);
        validateEditWindow(message);

        message.setContent(newContent);
        Message saved = messageRepository.save(message);

        return WsMessageEvent.builder()
                .type(WsEventType.EDIT)
                .messageId(saved.getId())
                .chatId(saved.getChat().getId())
                .content(saved.getContent())
                .sender(profileMapper.toSummary(profile))
                .deleted(false)
                .timestamp(saved.getUpdatedAt())
                .build();
    }

    /**
     * Soft-deletes a message and returns the broadcast event.
     * Sender or chat admin can delete.
     */
    @Transactional
    public WsMessageEvent deleteViaWebSocket(String username, UUID messageId) {
        User user = getUserByUsernameOrThrow(username);
        Message message = getMessageOrThrow(messageId);
        Profile profile = getProfileOrThrow(user);
        validateCanDelete(profile, message);

        Profile originalSender = message.getSender();
        softDelete(message);
        Message saved = messageRepository.save(message);

        return WsMessageEvent.builder()
                .type(WsEventType.DELETE)
                .messageId(saved.getId())
                .chatId(saved.getChat().getId())
                .content(null)
                .sender(profileMapper.toSummary(originalSender))
                .deleted(true)
                .timestamp(saved.getDeletedAt())
                .build();
    }

    // ================================================================
    // Private helpers
    // ================================================================

    private Message buildMessage(Chat chat, Profile profile, String content) {
        Message message = new Message();
        message.setChat(chat);
        message.setSender(profile);
        message.setContent(content);
        return message;
    }

    private void softDelete(Message message) {
        message.setDeleted(true);
        message.setDeletedAt(LocalDateTime.now());
        message.setContent(null);
    }

    private void validateMember(Profile profile, Chat chat) {
        if (!chat.isMember(profile)) {
            throw new ForbiddenException("You are not a member of this chat");
        }
    }

    private void validateSender(Profile profile, Message message) {
        if (!message.getSender().getId().equals(profile.getId())) {
            throw new UnauthorizeException("You cannot edit this message");
        }
    }

    private void validateCanDelete(Profile profile, Message message) {
        boolean isSender = message.getSender().getId().equals(profile.getId());
        boolean isAdmin  = message.getChat().isAdmin(profile);
        if (!isSender && !isAdmin) {
            throw new UnauthorizeException("You cannot delete this message");
        }
    }

    private void validateNotDeleted(Message message) {
        if (message.isDeleted()) {
            throw new ForbiddenException("Cannot edit a deleted message");
        }
    }

    private void validateEditWindow(Message message) {
        if (message.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(EDIT_WINDOW_MINUTES))) {
            throw new ForbiddenException(
                    "Messages can only be edited within " + EDIT_WINDOW_MINUTES + " minutes of being sent"
            );
        }
    }

    private Chat getChatOrThrow(UUID id) {
        return chatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found with id: " + id));
    }

    private Profile getProfileOrThrow(User user) {
        return profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + user.getId()));
    }

    private Message getMessageOrThrow(UUID id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + id));
    }

    private User getUserByUsernameOrThrow(String username) {
        return userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
