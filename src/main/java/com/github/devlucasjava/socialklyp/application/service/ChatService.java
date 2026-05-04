package com.github.devlucasjava.socialklyp.application.service;

import com.github.devlucasjava.socialklyp.application.dto.request.chat.CreateChatRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.chat.UpdateChatRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.chat.ChatResponse;
import com.github.devlucasjava.socialklyp.application.mapper.ChatMapper;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ConflictException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ForbiddenException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.ResourceNotFoundException;
import com.github.devlucasjava.socialklyp.delivery.rest.advice.UnauthorizeException;
import com.github.devlucasjava.socialklyp.domain.entity.Chat;
import com.github.devlucasjava.socialklyp.domain.entity.Profile;
import com.github.devlucasjava.socialklyp.domain.entity.User;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ChatRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.ProfileRepository;
import com.github.devlucasjava.socialklyp.infrastructure.database.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ChatMapper chatMapper;

    @Transactional(readOnly = true)
    public Page<ChatResponse> listMyChats(User auth, Pageable pageable) {
        Profile profile = getProfileOrThrow(auth);
        return chatRepository.findByMember(profile, pageable)
                .map(chatMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ChatResponse findChatById(User auth, UUID chatId) {
        Chat chat = getChatOrThrow(chatId);
        Profile profile = getProfileOrThrow(auth);

        validateMember(profile, chat);

        return chatMapper.toResponse(chat);
    }

    @Transactional
    public ChatResponse createChat(User auth, CreateChatRequest dto) {
        Profile profile = getProfileOrThrow(auth);

        Chat chat = chatMapper.toEntity(dto);
        chat.setProfileCreator(profile);
        chat.setProfiles(new HashSet<>());
        chat.setProfilesAdmin(new HashSet<>());

        chat.addAdminProfile(profile);
        chat.addMemberProfile(profile);

        return chatMapper.toResponse(chatRepository.save(chat));
    }

    @Transactional
    public ChatResponse updateChat(User auth, UUID chatId, UpdateChatRequest dto) {
        Chat chat = getChatOrThrow(chatId);
        Profile profile = getProfileOrThrow(auth);

        validateAdmin(profile, chat);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            chat.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            chat.setDescription(dto.getDescription());
        }

        return chatMapper.toResponse(chatRepository.save(chat));
    }

    @Transactional
    public void deleteChat(User auth, UUID chatId) {
        Chat chat = getChatOrThrow(chatId);
        Profile profile = getProfileOrThrow(auth);

        if (!chat.getProfileCreator().getId().equals(profile.getId())) {
            throw new UnauthorizeException("Only the chat creator can delete it");
        }

        chatRepository.deleteById(chatId);
    }

    @Transactional
    public ChatResponse addMember(User auth, UUID chatId, UUID targetProfileId) {
        Chat chat = getChatOrThrow(chatId);
        Profile admin = getProfileOrThrow(auth);

        validateAdmin(admin, chat);

        Profile target = getProfileByIdOrThrow(targetProfileId);

        if (chat.isMember(target)) {
            throw new ConflictException("Profile is already a member of this chat");
        }

        if (chat.isFull()) {
            throw new ConflictException("Chat has reached the maximum limit of " + Chat.MAX_MEMBERS + " members");
        }

        chat.addMemberProfile(target);
        return chatMapper.toResponse(chatRepository.save(chat));
    }

    @Transactional
    public ChatResponse removeMember(User auth, UUID chatId, UUID targetProfileId) {
        Chat chat = getChatOrThrow(chatId);
        Profile admin = getProfileOrThrow(auth);

        validateAdmin(admin, chat);

        Profile target = getProfileByIdOrThrow(targetProfileId);

        if (chat.getProfileCreator().getId().equals(target.getId())) {
            throw new ForbiddenException("Cannot remove the chat creator");
        }

        chat.getProfiles().remove(target);
        chat.getProfilesAdmin().remove(target);

        return chatMapper.toResponse(chatRepository.save(chat));
    }

    @Transactional
    public ChatResponse promoteToAdmin(User auth, UUID chatId, UUID targetProfileId) {
        Chat chat = getChatOrThrow(chatId);
        Profile admin = getProfileOrThrow(auth);

        validateAdmin(admin, chat);

        Profile target = getProfileByIdOrThrow(targetProfileId);

        if (!chat.isMember(target)) {
            throw new ForbiddenException("Profile is not a member of this chat");
        }

        chat.addAdminProfile(target);
        return chatMapper.toResponse(chatRepository.save(chat));
    }

    private void validateMember(Profile profile, Chat chat) {
        if (!chat.isMember(profile)) {
            throw new ForbiddenException("You are not a member of this chat");
        }
    }

    private void validateAdmin(Profile profile, Chat chat) {
        if (!chat.isAdmin(profile)) {
            throw new UnauthorizeException("You are not an admin of this chat");
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

    private Profile getProfileByIdOrThrow(UUID id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found with id: " + id));
    }

    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}