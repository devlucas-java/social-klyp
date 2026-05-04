package com.github.devlucasjava.socialklyp.unit.service;

import com.github.devlucasjava.socialklyp.application.dto.request.chat.CreateChatRequest;
import com.github.devlucasjava.socialklyp.application.dto.request.chat.UpdateChatRequest;
import com.github.devlucasjava.socialklyp.application.dto.response.chat.ChatResponse;
import com.github.devlucasjava.socialklyp.application.mapper.ChatMapper;
import com.github.devlucasjava.socialklyp.application.service.ChatService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock private ChatRepository chatRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private UserRepository userRepository;
    @Mock private ChatMapper chatMapper;

    private User authUser;
    private Profile creatorProfile;
    private Profile memberProfile;
    private Profile outsiderProfile;
    private Chat chat;
    private ChatResponse chatResponse;

    private final UUID chatId       = UUID.randomUUID();
    private final UUID creatorId    = UUID.randomUUID();
    private final UUID memberId     = UUID.randomUUID();
    private final UUID outsiderId   = UUID.randomUUID();

    @BeforeEach
    void setup() {
        authUser = new User();
        authUser.setId(UUID.randomUUID());

        creatorProfile = new Profile();
        creatorProfile.setId(creatorId);
        creatorProfile.setDisplayName("Creator");

        memberProfile = new Profile();
        memberProfile.setId(memberId);
        memberProfile.setDisplayName("Member");

        outsiderProfile = new Profile();
        outsiderProfile.setId(outsiderId);
        outsiderProfile.setDisplayName("Outsider");

        chat = new Chat();
        chat.setId(chatId);
        chat.setName("Dev Team");
        chat.setDescription("Dev chat");
        chat.setProfileCreator(creatorProfile);
        chat.setProfiles(new HashSet<>(Set.of(memberProfile)));
        chat.setProfilesAdmin(new HashSet<>());
        chat.setCreatedAt(LocalDateTime.now());
        chat.setUpdatedAt(LocalDateTime.now());

        chatResponse = ChatResponse.builder()
                .id(chatId)
                .name("Dev Team")
                .build();
    }

    // -------------------- LIST MY CHATS --------------------

    @Test
    void shouldListMyChatsSuccessfully() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Chat> page = new PageImpl<>(List.of(chat));

        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(creatorProfile));
        when(chatRepository.findByMember(creatorProfile, pageable)).thenReturn(page);
        when(chatMapper.toResponse(chat)).thenReturn(chatResponse);

        Page<ChatResponse> result = chatService.listMyChats(authUser, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnListMyChats() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> chatService.listMyChats(authUser, PageRequest.of(0, 10)));
    }

    // -------------------- FIND BY ID --------------------

    @Test
    void shouldFindChatByIdForMember() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(memberProfile));
        when(chatMapper.toResponse(chat)).thenReturn(chatResponse);

        ChatResponse result = chatService.findChatById(authUser, chatId);

        assertNotNull(result);
        assertEquals(chatId, result.getId());
    }

    @Test
    void shouldFindChatByIdForCreator() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(creatorProfile));
        when(chatMapper.toResponse(chat)).thenReturn(chatResponse);

        assertNotNull(chatService.findChatById(authUser, chatId));
    }

    @Test
    void shouldThrowWhenNonMemberTriesToFindChat() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(outsiderProfile));

        assertThrows(ForbiddenException.class,
                () -> chatService.findChatById(authUser, chatId));
    }

    @Test
    void shouldThrowWhenChatNotFoundOnFindById() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> chatService.findChatById(authUser, chatId));
    }

    // -------------------- CREATE --------------------

    @Test
    void shouldCreateChatSuccessfully() {
        CreateChatRequest request = new CreateChatRequest("Dev Team", "Dev chat");

        Chat newChat = new Chat();
        newChat.setId(chatId);
        newChat.setName("Dev Team");
        newChat.setProfiles(new HashSet<>());
        newChat.setProfilesAdmin(new HashSet<>());

        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(creatorProfile));
        when(chatMapper.toEntity(request)).thenReturn(newChat);
        when(chatRepository.save(any(Chat.class))).thenReturn(newChat);
        when(chatMapper.toResponse(newChat)).thenReturn(chatResponse);

        ChatResponse result = chatService.createChat(authUser, request);

        assertNotNull(result);
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void shouldThrowWhenProfileNotFoundOnCreate() {
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> chatService.createChat(authUser, new CreateChatRequest("name", null)));
    }

    // -------------------- UPDATE --------------------

    @Test
    void shouldUpdateChatSuccessfully() {
        // creator is also admin (via isAdmin logic)
        chat.setProfileCreator(creatorProfile);
        UpdateChatRequest request = new UpdateChatRequest("New Name", "New Desc");

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(creatorProfile));
        when(chatRepository.save(chat)).thenReturn(chat);
        when(chatMapper.toResponse(chat)).thenReturn(chatResponse);

        ChatResponse result = chatService.updateChat(authUser, chatId, request);

        assertNotNull(result);
        assertEquals("New Name", chat.getName());
        assertEquals("New Desc", chat.getDescription());
    }

    @Test
    void shouldNotUpdateNameWhenBlank() {
        chat.setProfileCreator(creatorProfile);
        UpdateChatRequest request = new UpdateChatRequest("   ", "New Desc");

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(creatorProfile));
        when(chatRepository.save(chat)).thenReturn(chat);
        when(chatMapper.toResponse(chat)).thenReturn(chatResponse);

        chatService.updateChat(authUser, chatId, request);

        assertEquals("Dev Team", chat.getName()); // unchanged
    }

    @Test
    void shouldThrowWhenNonAdminTriesToUpdate() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(outsiderProfile));

        assertThrows(UnauthorizeException.class,
                () -> chatService.updateChat(authUser, chatId, new UpdateChatRequest("x", null)));
    }

    // -------------------- DELETE --------------------

    @Test
    void shouldDeleteChatSuccessfully() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(creatorProfile));

        chatService.deleteChat(authUser, chatId);

        verify(chatRepository).deleteById(chatId);
    }

    @Test
    void shouldThrowWhenNonCreatorTriesToDelete() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(memberProfile));

        assertThrows(UnauthorizeException.class,
                () -> chatService.deleteChat(authUser, chatId));

        verify(chatRepository, never()).deleteById(any());
    }

    // -------------------- ADD MEMBER --------------------

    @Test
    void shouldAddMemberSuccessfully() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(creatorProfile));
        when(profileRepository.findById(outsiderId)).thenReturn(Optional.of(outsiderProfile));
        when(chatRepository.save(chat)).thenReturn(chat);
        when(chatMapper.toResponse(chat)).thenReturn(chatResponse);

        ChatResponse result = chatService.addMember(authUser, chatId, outsiderId);

        assertNotNull(result);
        assertTrue(chat.getProfiles().contains(outsiderProfile));
    }

    @Test
    void shouldThrowWhenAddingAlreadyMember() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(creatorProfile));
        when(profileRepository.findById(memberId)).thenReturn(Optional.of(memberProfile));

        assertThrows(ConflictException.class,
                () -> chatService.addMember(authUser, chatId, memberId));
    }

    @Test
    void shouldThrowWhenChatIsFullOnAddMember() {
        // fill chat to MAX_MEMBERS
        Set<Profile> bigSet = new HashSet<>();
        for (int i = 0; i < Chat.MAX_MEMBERS; i++) {
            Profile p = new Profile();
            p.setId(UUID.randomUUID());
            bigSet.add(p);
        }
        chat.setProfiles(bigSet);

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(creatorProfile));
        when(profileRepository.findById(outsiderId)).thenReturn(Optional.of(outsiderProfile));

        assertThrows(ConflictException.class,
                () -> chatService.addMember(authUser, chatId, outsiderId));
    }

    @Test
    void shouldThrowWhenNonAdminTriesToAddMember() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(outsiderProfile));

        assertThrows(UnauthorizeException.class,
                () -> chatService.addMember(authUser, chatId, UUID.randomUUID()));
    }

    // -------------------- REMOVE MEMBER --------------------

    @Test
    void shouldRemoveMemberSuccessfully() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(creatorProfile));
        when(profileRepository.findById(memberId)).thenReturn(Optional.of(memberProfile));
        when(chatRepository.save(chat)).thenReturn(chat);
        when(chatMapper.toResponse(chat)).thenReturn(chatResponse);

        chatService.removeMember(authUser, chatId, memberId);

        assertFalse(chat.getProfiles().contains(memberProfile));
    }

    @Test
    void shouldThrowWhenTryingToRemoveCreator() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(creatorProfile));
        when(profileRepository.findById(creatorId)).thenReturn(Optional.of(creatorProfile));

        assertThrows(ForbiddenException.class,
                () -> chatService.removeMember(authUser, chatId, creatorId));
    }

    // -------------------- PROMOTE TO ADMIN --------------------

    @Test
    void shouldPromoteMemberToAdminSuccessfully() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(creatorProfile));
        when(profileRepository.findById(memberId)).thenReturn(Optional.of(memberProfile));
        when(chatRepository.save(chat)).thenReturn(chat);
        when(chatMapper.toResponse(chat)).thenReturn(chatResponse);

        chatService.promoteToAdmin(authUser, chatId, memberId);

        assertTrue(chat.getProfilesAdmin().contains(memberProfile));
    }

    @Test
    void shouldThrowWhenPromotingNonMember() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(profileRepository.findByUser(authUser)).thenReturn(Optional.of(creatorProfile));
        when(profileRepository.findById(outsiderId)).thenReturn(Optional.of(outsiderProfile));

        assertThrows(ForbiddenException.class,
                () -> chatService.promoteToAdmin(authUser, chatId, outsiderId));
    }
}
