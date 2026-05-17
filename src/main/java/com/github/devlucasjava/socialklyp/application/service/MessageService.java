package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.request.message.EditMessageRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.message.SendMessageRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.message.MessageResponse;
import com.github.devlucasjava.socialklyp.application.mapper.MessageMapper;
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

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ProfileRepository profileRepository;
    private final MessageMapper messageMapper;

    @Transactional(readOnly = true)
    public Page<MessageResponse> findByChatId(User auth, UUID chatId, Pageable pageable) {
        Chat chat = getChatOrThrow(chatId);
        Profile profile = getProfileOrThrow(auth);

        validateMember(profile, chat);

        return messageRepository.findByChatId(chatId, pageable)
                .map(messageMapper::toResponse);
    }

    @Transactional
    public MessageResponse send(User auth, UUID chatId, SendMessageRequest request) {
        Chat chat = getChatOrThrow(chatId);
        Profile profile = getProfileOrThrow(auth);

        validateMember(profile, chat);

        Message message = new Message();
        message.setChat(chat);
        message.setSender(profile);
        message.setContent(request.content());

        return messageMapper.toResponse(messageRepository.save(message));
    }

    @Transactional
    public MessageResponse edit(User auth, UUID messageId, EditMessageRequest request) {
        Message message = getMessageOrThrow(messageId);
        Profile profile = getProfileOrThrow(auth);

        validateSender(profile, message);

        if (message.isDeleted()) {
            throw new ForbiddenException("Cannot edit a deleted message");
        }

        message.setContent(request.content());

        return messageMapper.toResponse(messageRepository.save(message));
    }

    @Transactional
    public void delete(User auth, UUID messageId) {
        Message message = getMessageOrThrow(messageId);
        Profile profile = getProfileOrThrow(auth);

        boolean isSender = message.getSender().getId().equals(profile.getId());
        boolean isAdmin  = message.getChat().isAdmin(profile);

        if (!isSender && !isAdmin) {
            throw new UnauthorizeException("You cannot delete this message");
        }

        message.setDeleted(true);
        message.setDeletedAt(LocalDateTime.now());
        message.setContent(null);

        messageRepository.save(message);
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
}
