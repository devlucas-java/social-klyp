package com.github.devlucasjava.socialklyp.unit.service;

import com.github.devlucasjava.socialklyp.application.dto.request.message.EditMessageRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.message.SendMessageRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.message.MessageResponse;
import com.github.devlucasjava.socialklyp.application.mapper.MessageMapper;
import com.github.devlucasjava.socialklyp.application.service.MessageService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @InjectMocks
    private MessageService messageService;

    @Mock private MessageRepository messageRepository;
    @Mock private ChatRepository chatRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private MessageMapper messageMapper;

    private User authUser;
    private Profile senderProfile;
    private Profile memberProfile;
    private Profile outsiderProfile;
    private Profile adminProfile;
    private Chat chat;
    private Message message;
    private MessageResponse messageResponse;

    private final UUID chatId      = UUID.randomUUID();
    private final UUID messageId   = UUID.randomUUID();
    private final UUID senderId    = UUID.randomUUID();
    private final UUID memberId    = UUID.randomUUID();
    private final UUID outsiderId  = UUID.randomUUID();
    private final UUID adminId     = UUID.randomUUID();

    @BeforeEach
    void setup() {
        authUser = new User();
        authUser.setId(UUID.randomUUID());

        senderProfile = new Profile();
        senderProfile.setId(senderId);

        memberProfile = new Profile();
        memberProfile.setId(memberId);

        outsiderProfile = new Profile();
        outsiderProfile.setId(outsiderId);

        adminProfile = new Profile();
        adminProfile.setId(adminId);

        chat = new Chat();
        chat.setId(chatId);
        chat.setName("Dev Team");
        chat.setProfileCreator(adminProfile);
        chat.setProfiles(new HashSet<>(Set.of(senderProfile, memberProfile)));
        chat.setProfilesAdmin(new HashSet<>());

        message = new Message();
        message.setId(messageId);
        message.setChat(chat);
        message.setSender(senderProfile);
        message.setContent("Hello!");
        message.setDeleted(false);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        messageResponse = MessageResponse.builder()
                .id(messageId)
                .chatId(chatId)
                .content("Hello!")
                .deleted(false)
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }

    // -------------------- FIND BY CHAT ID --------------------

    @Test
    void shouldFindMessagesByChatIdForMember() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Message> page = new PageImpl<>(List.of(message));

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(senderProfile));
        when(messageRepository.findByChatId(chatId, pageable)).thenReturn(page);
        when(messageMapper.toResponse(message)).thenReturn(messageResponse);

        Page<MessageResponse> result = messageService.findByChatId(authUser, chatId, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(messageId, result.getContent().get(0).getId());
    }

    @Test
    void shouldThrowWhenNonMemberTriesToReadMessages() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(outsiderProfile));

        assertThrows(ForbiddenException.class,
                () -> messageService.findByChatId(authUser, chatId, PageRequest.of(0, 20)));
    }

    @Test
    void shouldThrowWhenChatNotFoundOnFindMessages() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> messageService.findByChatId(authUser, chatId, PageRequest.of(0, 20)));
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnFindMessages() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> messageService.findByChatId(authUser, chatId, PageRequest.of(0, 20)));
    }

    // -------------------- SEND --------------------

    @Test
    void shouldSendMessageSuccessfully() {
        SendMessageRequest request = new SendMessageRequest("Hello everyone!");

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(senderProfile));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(messageMapper.toResponse(message)).thenReturn(messageResponse);

        MessageResponse result = messageService.send(authUser, chatId, request);

        assertNotNull(result);
        assertEquals(messageId, result.getId());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void shouldSaveCorrectContentOnSend() {
        SendMessageRequest request = new SendMessageRequest("Test content");

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(senderProfile));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(messageMapper.toResponse(message)).thenReturn(messageResponse);

        messageService.send(authUser, chatId, request);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertEquals("Test content", captor.getValue().getContent());
        assertEquals(senderProfile, captor.getValue().getSender());
        assertEquals(chat, captor.getValue().getChat());
    }

    @Test
    void shouldThrowWhenNonMemberTriesToSend() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(outsiderProfile));

        assertThrows(ForbiddenException.class,
                () -> messageService.send(authUser, chatId, new SendMessageRequest("Hi")));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenChatNotFoundOnSend() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> messageService.send(authUser, chatId, new SendMessageRequest("Hi")));
    }

    // -------------------- EDIT --------------------

    @Test
    void shouldEditMessageSuccessfully() {
        EditMessageRequest request = new EditMessageRequest("Updated content");
        MessageResponse updatedResponse = MessageResponse.builder()
                .id(messageId).content("Updated content").build();

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(senderProfile));
        when(messageRepository.save(message)).thenReturn(message);
        when(messageMapper.toResponse(message)).thenReturn(updatedResponse);

        MessageResponse result = messageService.edit(authUser, messageId, request);

        assertEquals("Updated content", message.getContent());
        assertNotNull(result);
    }

    @Test
    void shouldThrowWhenNonSenderTriesToEdit() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(memberProfile));

        assertThrows(UnauthorizeException.class,
                () -> messageService.edit(authUser, messageId, new EditMessageRequest("x")));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenEditingDeletedMessage() {
        message.setDeleted(true);

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(senderProfile));

        assertThrows(ForbiddenException.class,
                () -> messageService.edit(authUser, messageId, new EditMessageRequest("x")));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenMessageNotFoundOnEdit() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> messageService.edit(authUser, messageId, new EditMessageRequest("x")));
    }

    // -------------------- DELETE --------------------

    @Test
    void shouldDeleteMessageBySenderSuccessfully() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(senderProfile));
        when(messageRepository.save(message)).thenReturn(message);

        messageService.delete(authUser, messageId);

        assertTrue(message.isDeleted());
        assertNull(message.getContent());
        assertNotNull(message.getDeletedAt());
        verify(messageRepository).save(message);
    }

    @Test
    void shouldDeleteMessageByChatAdminSuccessfully() {
        // adminProfile is the chat creator → isAdmin = true
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(adminProfile));
        when(messageRepository.save(message)).thenReturn(message);

        messageService.delete(authUser, messageId);

        assertTrue(message.isDeleted());
        assertNull(message.getContent());
    }

    @Test
    void shouldThrowWhenNeitherSenderNorAdminTriesToDelete() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(outsiderProfile));

        assertThrows(UnauthorizeException.class,
                () -> messageService.delete(authUser, messageId));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenMessageNotFoundOnDelete() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> messageService.delete(authUser, messageId));
    }

    @Test
    void shouldSetDeletedAtTimestampOnDelete() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(senderProfile));
        when(messageRepository.save(message)).thenReturn(message);

        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        messageService.delete(authUser, messageId);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertTrue(message.getDeletedAt().isAfter(before));
        assertTrue(message.getDeletedAt().isBefore(after));
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnDelete() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> messageService.delete(authUser, messageId));
    }
}
